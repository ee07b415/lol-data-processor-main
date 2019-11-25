package apiparser.job

import apiparser.model.transformed.FlattenJobPubsubMessge
import apiparser.processor.{FlattenMatchDataByLaneAgainst, FlattenMatchDataByTeamBan}
import apiparser.util.{BigQueryUtil, CredentialUtil, PubsubAgent}
import scala.collection.mutable

/**
 * Run in pants:
 * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=SaveFlattenedMatch --future-config='justise come to all'"
 *
 * Run with Jar:
 *
 * java -cp dist/apiparser.jar apiparser.Main "--job-name=SaveFlattenedMatch" "--future-config=justise come to all"
 *
 */
class SaveFlattenedMatch extends GeneralJob {
  override def whoami(): Unit = {
    println("print from save flatthened match job")
  }

  /**
   * grab data from gcs and save the the last match name somewhere, compare the match name time stamp
   * only do bq write when new time stamp greater than last time
   * @param jobArgs
   */
  override def execute(jobArgs: JobArgs): Unit = {
    val bigquery = new BigQueryUtil(config.gcp.projectId, CredentialUtil.getCredential)

    //grab task
    val messageList = new mutable.ListBuffer[FlattenJobPubsubMessge]
    val pubsub = new PubsubAgent[FlattenJobPubsubMessge](config.gcp.projectId, CredentialUtil.getCredential)
    pubsub.subscribe("match-job-listener", 20)
    pubsub.receivedMessageList
      .foreach(message => {
        println(message.getFilePath)
        messageList.append(message)
      })

    //insertion
    messageList.foreach(message => {
      val flattenedData = new FlattenMatchDataByLaneAgainst()
      val flatData = flattenedData.getFomGcs(config.gcp.projectId, message.bucket, message.getDateFilePath)
      val schema = flatData.head.makeTableSchema

//      bigquery.createTable("test_dataset", "lane_assignments", schema)
//      println("table create success!")

      val rows = flatData.map(x => x.buildTableRow)

      bigquery.write("test_dataset", "lane_assignments", rows)

      println(s"${rows.size} of rows appended")
    })
  }
}
