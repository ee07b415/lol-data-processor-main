package apiparser.job

import apiparser.util.{BitMapFactory, BitMapManager, SeedManagerFactory}
import com.google.common.base.Preconditions
import java.io.{FileNotFoundException, IOException}
import scala.collection.mutable

/**
  * should not run too often since we will get the new seed from the grabmatch job, this should only
  * run once on the first day or some how the seed not ready from grabmatch
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=SeedGenerate"
  *
  * Run with Jar:
  *
  * java -cp dist/apiparser.jar apiparser.Main "--job-name=SeedGenerate" "--future-config=justise come to all"
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
class SeedGenerator extends CrawlerJob {

  /**
    * get the match id list from a list of user account id
    * @param seedUserAccount user account id, perform as the seed on recursive get
    * @return
    */
  def getData(seedUserAccount: List[String],
              bitMapManager: BitMapManager): List[String] = {
    val matchBitMap = bitMapManager.get
    val seedMatchIdList = new mutable.ListBuffer[String]() // returned data
    val summonerAccountSet = new mutable.HashSet[String]() // for dedup purpose
    val intermediaMatchQueue = new mutable.Queue[String]()
    val intermediasummonerAccountQueue = new mutable.Queue[String]()

    var loopControl = 0

    summonerAccountSet.++=(seedUserAccount)
    intermediasummonerAccountQueue.++=(seedUserAccount.toSet)

    //how many matches from each user account we are pulling
    val endIndex = Math.max(1, cutoff / seedUserAccount.size)

    while (intermediasummonerAccountQueue.nonEmpty && seedMatchIdList.size < cutoff && loopControl < cutoff) {
      // get the match list from one seed player account id
      try {
        val matchList = getMatchListByAccountId(intermediasummonerAccountQueue.dequeue(), endIndex)
          .matches
          .map(_.gameId)
          .toSet
        matchList.foreach(matchId => {
          if (!seedMatchIdList.contains(matchId.toString) && !matchBitMap
                .contains(matchId)) {
            seedMatchIdList.append(matchId.toString)
            intermediaMatchQueue.enqueue(matchId.toString)
          }
        })

        //if not enough, get more player account id from the matches
        if (seedMatchIdList.size < cutoff && intermediasummonerAccountQueue.isEmpty) {
          while (intermediasummonerAccountQueue.isEmpty && intermediaMatchQueue.nonEmpty) {
            val matchTemp = getMatchByMatchId(intermediaMatchQueue.dequeue())
            val summonerList = matchTemp.participantIdentities
              .map(_.player.accountId)
              .toSet

            summonerList.foreach(accountId => {
              if (!summonerAccountSet.contains(accountId)) {
                summonerAccountSet.add(accountId)
                intermediasummonerAccountQueue.enqueue(accountId)
              }
            })
          }
        }
      } catch {
        case e: FileNotFoundException =>
          println("Bad match just skip")

        case e: IOException =>
          if (e.getMessage.contains("Server returned HTTP response code: 429")) {
            println(
              s"Rate limit, lets cut off at ${seedMatchIdList.size} by this time")
            loopControl = cutoff
          }
      }

      loopControl = loopControl + 1
    }

    seedMatchIdList.toList
  }

  override def whoami(): Unit = {
    println("print from seed generator job")
  }

  override def execute(jobArgs: JobArgs): Unit = {
    val userNameSeed = config.seed
      .map(userName => {
        getSummonerByName(userName).accountId
      })

    Preconditions.checkArgument(userNameSeed.nonEmpty)

    val bitMapManager = BitMapFactory.getBitMapManager(config)
    val seedManager = SeedManagerFactory.getSeedManager(config)

    //Match already parsed
    val matchIdList = getData(userNameSeed, bitMapManager)
    seedManager.set(matchIdList)
  }
}
