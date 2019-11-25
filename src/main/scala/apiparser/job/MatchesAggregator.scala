package apiparser.job

import apiparser.model.MatchDto
import apiparser.util.{CredentialUtil, FileUtil, GcsUtil, JsonUtil}
import scala.collection.mutable
import scala.util.matching.Regex

/**
 * Group the match list files into a bigger aggregated file, reduce the time from read and write to gcp cloud
 *
 * Run in pants:
 * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=GrabMatch"
 *
 * Run with Jar:
 *
 * java -cp dist/apiparser.jar apiparser.Main "--job-name=GrabMatch" "--future-config=justise come to all"
 */
class MatchesAggregator extends GeneralJob {
  val fileNamePattern: Regex = raw"(\d{4})/(\d{2})/(\d{2})/match_(\d{12}).json".r
  val cutoff = 500//1000 will result in java.lang.OutOfMemoryError: Java heap space

  override def whoami(): Unit = {
    println("This is match aggregator job")
  }

  override def execute(jobArgs: JobArgs): Unit = {
    val gcs = new GcsUtil(config.gcp.projectId, CredentialUtil.getCredential)
    val pathList = gcs.expand(config.gcp.matchBucket, pattern = fileNamePattern).toIterator

    val matchList = new mutable.ListBuffer[MatchDto]

    while(matchList.size < cutoff && pathList.hasNext){
      val path = pathList.next()
      val remain = cutoff - matchList.size
      val matchData = JsonUtil.fromJson[Map[String, List[MatchDto]]](gcs.get(config.gcp.matchBucket, path))
      matchList.appendAll(matchData("matches").splitAt(remain)._1)
    }
    println(s"size:${matchList.size}")

    val matchJsonObj = Map("matches" -> matchList.toList)
    FileUtil.writeFile("match_local.json", JsonUtil.toJson(matchJsonObj))
  }
}
