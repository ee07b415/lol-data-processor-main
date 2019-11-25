package apiparser.job

import apiparser.model.MatchDto
import apiparser.common.Constant
import apiparser.model.transformed.FlattenJobPubsubMessge
import apiparser.util.{ApiUtil, CredentialUtil, FileUtil, GcsUtil, JsonUtil, PubsubAgent, Retry}
import com.google.protobuf.ByteString
import java.io.{FileNotFoundException, IOException}
import org.joda.time.{LocalDate, LocalDateTime}
import org.roaringbitmap.longlong.Roaring64NavigableMap
import scala.collection.mutable

/**
  *
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=MatchAggregator"
  *
  * Run with Jar:
  * java -cp dist/apiparser.jar apiparser.Main "--job-name=MatchAggregator" "--future-config=justise"
  *
  * example in main
  *
  * //  val jobArgs = Cli.parse(args).withCommand(new JobArgs)(parsed => parsed)
  * //  val injector = Guice.createInjector(new ApiParserModule())
  * //  val jobName = JobName.saveParse(jobArgs.get.jobName)
  * //  val job = injector.instance[GeneralJob](Names.named(jobName.toString))
  * //  val seed = job.getSummonerByName("RiotSchmick").accountId // riot's example account
  * //  val data = job.getData(List(seed))
  * //  print(data)
  */
class GrabMatch extends CrawlerJob {

  /**
    * refill seed list to at least seed_min for next time
    * @param intermediaMatchQueue
    * @param intermediasummonerAccountQueue
    * @param matchBitMap
    */
  def seedRefill(intermediaMatchQueue: mutable.Queue[String],
                 intermediasummonerAccountQueue: mutable.Queue[String],
                 matchBitMap: Roaring64NavigableMap,
                 gcs: GcsUtil): Unit = {
    var seedRefillDone = false
    if (intermediaMatchQueue.size < seed_min) {
      Retry.retry(12, 10) {
        if (!ApiUtil.rateLimitCheck)
          throw new IOException(
            "too fast, Server returned HTTP response code: 429")
      }
    }

    while (intermediaMatchQueue.size < seed_min && !seedRefillDone) {
      try {
        if (intermediasummonerAccountQueue.nonEmpty) {
          val matchList = getMatchListByAccountId(intermediasummonerAccountQueue.dequeue(), 20).matches
            .map(_.gameId)
            .toSet

          matchList.foreach(matchId => {
            if (!matchBitMap.contains(matchId)) {
              intermediaMatchQueue.enqueue(matchId.toString)
            }
          })
        } else {
          seedRefillDone = true
        }
      } catch {
        case e: FileNotFoundException =>
          println("Bad account just skip")

        case e: IOException =>
          if (e.getMessage.contains("Server returned HTTP response code: 429")) {
            println(
              s"Rate limit, lets cut off at ${intermediaMatchQueue.size} by this time")
            seedRefillDone = true
          }
      }
    }

    //overwrite existing seed
    gcs.save(config.gcp.seedBucket, "seed.csv", intermediaMatchQueue.mkString("\n"))
  }

  /**
    *
    * get the match id list from a list of match id
    *
    * @param seed matchId
    *
    * @return
    */
  def getData(seed: List[String],
              matchBitMap: Roaring64NavigableMap,
              gcs: GcsUtil): Map[String, MatchDto] = {
    val matchIdMap = new mutable.HashMap[String, MatchDto]() // returned data
    val summonerAccountSet = new mutable.HashSet[String]() // for dedup purpose
    val intermediaMatchQueue = new mutable.Queue[String]()
    val intermediasummonerAccountQueue = new mutable.Queue[String]()

    var loopControl = 0

    intermediaMatchQueue.++=(seed)

    while (intermediaMatchQueue.nonEmpty && matchIdMap.size < cutoff && loopControl < cutoff) {
      // get the match information
      try {
        val matchCandidate = intermediaMatchQueue.dequeue()
        if (!matchBitMap.contains(matchCandidate.toLong)) {
          val matchDto = getMatchByMatchId(matchCandidate)
          //lets focus on rank match for now
          if (matchDto.isMatchGame) {
            matchIdMap.put(matchDto.gameId.toString, matchDto)
            matchBitMap.add(matchDto.gameId)
          }
          val participants = matchDto.participantIdentities
            .map(_.player.accountId)
            .toSet
          participants.foreach { player =>
            if (!summonerAccountSet.contains(player)) {
              summonerAccountSet.add(player)
              intermediasummonerAccountQueue.enqueue(player)
            }
          }
        }

        //if not enough, get more match ids from the player account
        if (matchIdMap.size < cutoff && intermediaMatchQueue.isEmpty && intermediasummonerAccountQueue.nonEmpty) {
          val matchList = getMatchListByAccountId(intermediasummonerAccountQueue.dequeue(), 50)
            .matches
            .map(_.gameId)
            .toSet

          matchList.foreach(matchId => {
            if (!matchBitMap.contains(matchId)) {
              intermediaMatchQueue.enqueue(matchId.toString)
            }
          })
        }
      } catch {
        case e: FileNotFoundException =>
          println("Bad match just skip")

        case e: IOException =>
          if (e.getMessage.contains("Server returned HTTP response code: 429")) {
            println(
              s"Rate limit, lets cut off at ${matchIdMap.size} by this time")
            loopControl = cutoff
          }
      }

      loopControl = loopControl + 1
    }

    seedRefill(intermediaMatchQueue,
               intermediasummonerAccountQueue,
               matchBitMap,
               gcs)

    matchIdMap.toMap
  }

  def getSeed(seed: String, gcs: GcsUtil): List[String] = {
    gcs.get(config.gcp.seedBucket, seed).split("\n").toList
  }

  def publishTask(executionDate: String, fileName: String): Unit ={
    val message = new FlattenJobPubsubMessge(
      config.gcp.matchBucket,
      executionDate,
      fileName
    )

    val pubsub = new PubsubAgent[FlattenJobPubsubMessge](config.gcp.projectId, CredentialUtil.getCredential)
    pubsub.publish(config.gcp.matchPubTopic, ByteString.copyFromUtf8(JsonUtil.toJson(message)))
  }

  override def whoami(): Unit = {
    println("print from grabMatch job")
  }

  override def execute(jobArgs: JobArgs): Unit = {
    //Match already parsed
    val projectId = config.gcp.projectId
    val gcs = new GcsUtil(projectId, CredentialUtil.getCredential)

    val seed = getSeed("seed.csv", gcs)

    val matchBitMap =
      gcs.getMatchBitMap(config.gcp.bitMapbucket, "matchList.bin")
    val data = getData(seed, matchBitMap, gcs)

    val matchList = Map("matches" -> data.values.toList.splitAt(cutoff)._1)

    //for local check
    FileUtil.writeFile("match_local.json", JsonUtil.toJson(matchList))

    val datePath = LocalDate
      .parse(jobArgs.executionDate)
      .toString(Constant.gcsPathDateFormatter)

    val fileName = s"match_${LocalDateTime.now().toString(Constant.dateTimeFormatter)}.json"

    val matchGcsPath = s"$datePath/$fileName"

    gcs.save(config.gcp.matchBucket, matchGcsPath, JsonUtil.toJson(matchList))
    gcs.saveMatchBitMap(matchBitMap, config.gcp.bitMapbucket, "matchList.bin")
    publishTask(jobArgs.executionDate, fileName)
  }
}
