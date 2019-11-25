package apiparser.util

import com.google.cloud.bigquery.{
  BigQuery,
  BigQueryOptions,
  InsertAllRequest,
  InsertAllResponse,
  JobId,
  JobInfo,
  QueryJobConfiguration,
  Schema,
  StandardTableDefinition,
  TableId,
  TableInfo,
  TableResult
}
import com.google.auth.oauth2.GoogleCredentials
import java.util.UUID
import collection.JavaConverters._

/**
 * usage example:
 *
 * //  val projectId = ConfigUtil.getConfig.gcp.projectId
 * //  val bigquery = new BigQueryUtil(projectId, CredentialUtil.getCredential)
 * //
 * //  val query = "SELECT * FROM `${projectId}.test_dataset.win_rate_rank` LIMIT 1000"
 * //
 * //  val tableResult = bigquery.readByQuery(query)
 * //
 * //  tableResult.iterateAll()
 * //    .forEach(row => {
 * //      val winRateRank: WinRateRank = new WinRateRank()
 * //      winRateRank.readFromFieldList(row)
 * //      print(winRateRank.toString)
 * //    })
 *
 * //  val sorakaWinRate = new WinRateRank(champion = "soraka", win = 2, fail = 0, winRate = 1.0, failRate = 0.0)
 * //  val rows = new mutable.ListBuffer[InsertAllRequest.RowToInsert]
 * //  rows.append(sorakaWinRate.buildTableRow)
 * //  bigquery.write("test_dataset", "win_rate_rank", rows.toList)
 *
 * @param projectId
 * @param credentials
 */
class BigQueryUtil(val projectId: String, val credentials: GoogleCredentials) {

  val bigquery: BigQuery = BigQueryOptions.newBuilder
    .setProjectId(projectId)
    .setCredentials(credentials)
    .build
    .getService

  def readByQuery(query: String): TableResult = {

    val queryConfig =
      QueryJobConfiguration.newBuilder(query).setUseLegacySql(false).build

    // Create a job ID so that we can safely retry.
    val jobId = JobId.of(UUID.randomUUID.toString)

    var queryJob =
      bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build)

    // Wait for the query to complete.
    queryJob = queryJob.waitFor()

    // Check for errors
    if (queryJob == null) throw new RuntimeException("Job no longer exists")
    else if (queryJob.getStatus.getError != null) { // You can also look at queryJob.getStatus().getExecutionErrors() for all
      // errors, not just the latest one.
      throw new RuntimeException(queryJob.getStatus.getError.toString)
    }

    // Get the results.
    queryJob.getQueryResults()
  }

  def createTable(datasetName: String,
                  tableName: String,
                  schema: Schema): Unit = {
    val tableId = TableId.of(datasetName, tableName)
    val tableDefinition = StandardTableDefinition.of(schema)
    val tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build
    bigquery.create(tableInfo)
  }

  def write(
      datasetName: String,
      tableName: String,
      rowContent: List[InsertAllRequest.RowToInsert]
  ): Unit = {
    val tableId: TableId = TableId.of(datasetName, tableName)

    rowContent
      .grouped(500)// TODO: refine this later, it seems there is a length limit on the request
      .foreach(partitionedList => {
        val response: InsertAllResponse = bigquery
          .insertAll(
            InsertAllRequest
              .newBuilder(tableId)
              .setRows(partitionedList.asJava)
              .build // More rows can be added in the same RPC by invoking .addRow() on the builder.
            // You can also supply optional unique row keys to support de-duplication scenarios.
          )

        if (response.hasErrors) {
          response
            .getInsertErrors
            .entrySet()
            .forEach(entry => {
              entry.getValue
                .forEach(value => {
                  print(s"${entry.getKey} failed with: ${value.getReason}")
                })
            })
        }
      })
  }
}
