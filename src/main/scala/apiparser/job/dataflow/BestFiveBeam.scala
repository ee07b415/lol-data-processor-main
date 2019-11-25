package apiparser.job.dataflow

import apiparser.common.Constant
import apiparser.job.{GeneralJob, JobArgs}
import apiparser.model.MatchDto
import apiparser.model.transformed.{TeamWinRate, TeamWinRateBuilder}
import apiparser.processor.FlattenMatchDataByTeamLane
import apiparser.util.{CredentialUtil, GcsUtil, JsonUtil}
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.transforms.{Create, DoFn, MapElements, ParDo, SimpleFunction}
import scala.util.matching.Regex
import collection.JavaConverters._
import com.google.api.services.bigquery.model.{TableReference, TableRow}
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.{CreateDisposition, WriteDisposition}
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.joda.time.{Duration, LocalDateTime}

/**
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestFiveBeam --future-config='justise'"
  *
  * Run with Jar:
  *
  * java -cp dist/apiparser.jar apiparser.Main "--job-name=BestFiveBeam" "--future-config=justise come to all"
  *
  */
class BestFiveBeam extends GeneralJob {
  override def whoami(): Unit = {
    println("This is print from best five dataflow job")
  }

  private final class ReadMatches extends DoFn[String, MatchDto] {
    @ProcessElement
    def ProcessElement(c:ProcessContext) {
      // Use OutputReceiver.output to emit the output element.
      val stringContent = c.element()
      val matchData = JsonUtil.fromJson[Map[String, List[MatchDto]]](stringContent)

      matchData("matches")
        .foreach(matchDto => {
          if (matchDto.isMatchGame){
            c.output(matchDto)
          }
        })
    }
  }

  private final class ToTeamWinRate extends DoFn[MatchDto, TeamWinRate] {
    @ProcessElement
    def ProcessElement(c:ProcessContext) {
      // Use OutputReceiver.output to emit the output element.
      val matchDto = c.element()
      val dataProcessor = new FlattenMatchDataByTeamLane()
      val data = dataProcessor.flattenData(matchDto)
      println(s"This is ${matchDto.gameId}")
      data
        .foreach(game => {
          c.output(new TeamWinRate(
            game.teamOneTopChampionId,
            game.teamOneJungleChampionId,
            game.teamOneMidChampionId,
            game.teamOneCarryChampionId,
            game.teamOneSupportChampionId,
            1,
            if (game.winTeamId == 100) 1 else 0,
            game.gameId))
          c.output(new TeamWinRate(
            game.teamTwoTopChampionId,
            game.teamTwoJungleChampionId,
            game.teamTwoMidChampionId,
            game.teamTwoCarryChampionId,
            game.teamTwoSupportChampionId,
            1,
            if (game.winTeamId == 200) 1 else 0,
            game.gameId))
        })
    }
  }

  override def execute(jobArgs: JobArgs): Unit = {
    val gcpCredential = CredentialUtil.getCredential

//    val fileNamePattern: Regex = raw"(\d{4})/(\d{2})/(\d{2})/match_(\d{12}).json".r
//    val gcs = new GcsUtil(config.gcp.projectId, gcpCredential)
//    val pathList = gcs
//      .expand(config.gcp.matchBucket, pattern = fileNamePattern)
//        .map(path => s"gs://${config.gcp.matchBucket}/$path")

    val tableSpec =
      new TableReference()
        .setProjectId(config.gcp.projectId)
        .setDatasetId("test_dataset")
        .setTableId("best_five")

    val tableSchema = TeamWinRateBuilder.getDefaultTeamWin.makeBigQueryTableSchema
    val args = jobArgs.dataflowJobArgs.split(" ")
    val options = PipelineOptionsFactory.fromArgs(args: _*).create().as(classOf[GcpOptions])
    options.setJobName(jobArgs.jobName + LocalDateTime.now().toString(Constant.dateTimeFormatter))
    options.setProject(config.gcp.projectId)
    options.setGcpCredential(gcpCredential)
    val p = Pipeline.create(options)

    p
      .apply("Read file", TextIO.read().withHintMatchesManyFiles().from("gs://match_info/2019/*/*/*.json"))
      .apply("Parse string", ParDo.of(new ReadMatches()))
      .apply("Transform to team lan win", ParDo.of(new ToTeamWinRate()))
      .apply("To table row",
        MapElements.via(new SimpleFunction[TeamWinRate, TableRow](){
          override def apply(input:TeamWinRate): TableRow ={
            input.toBigQueryTableRow
          }
        }))
        .apply(BigQueryIO.writeTableRows()
          .to(tableSpec)
          .withSchema(tableSchema)
          .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
          .withWriteDisposition(WriteDisposition.WRITE_TRUNCATE))

    p.run().waitUntilFinish(Duration.standardMinutes(10))
  }
}
