package apiparser.job.dataflow

import apiparser.common.Constant
import apiparser.job.{GeneralJob, JobArgs}
import apiparser.model.transformed.{TeamWinRate, TeamWinRateBuilder}
import apiparser.util.{ChampionUtil, ConfigUtil, CredentialUtil, JsonUtil}
import com.google.api.services.bigquery.model.{TableFieldSchema, TableReference, TableRow, TableSchema}
import com.google.cloud.bigquery.LegacySQLTypeName
import java.util
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.{KvCoder, StringUtf8Coder}
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions
import org.apache.beam.sdk.io.gcp.bigquery.{BigQueryIO, SchemaAndRecord, TableRowJsonCoder}
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.{CreateDisposition, WriteDisposition}
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.{DoFn, GroupByKey, MapElements, ParDo, SerializableFunction, SimpleFunction}
import org.apache.beam.sdk.values.KV
import org.joda.time.{Duration, LocalDateTime}

/**
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='MIDDLE,JUNGLE'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='TOP,JUNGLE'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='TOP,MIDDLE,JUNGLE'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='CARRY,SUPPORT'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='TOP'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='JUNGLE'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='MIDDLE'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='CARRY'"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestPartyBeam --combination='SUPPORT'"
  *
  * Run with Jar:
  *
  * java -cp apiparser.jar apiparser.Main "--job-name=BestPartyBeam" "--combination=TOP,JUNGLE,MIDDLE"
  *
  */
class BestPartyBeam extends GeneralJob {
  override def whoami(): Unit = {
    println("This is print from best five dataflow job")
  }

  private final class Aggregation(taskOfThisJob: List[String], championIndexToNameMap:Map[Int, String])
      extends DoFn[KV[String, java.lang.Iterable[String]], TableRow] {
    @ProcessElement
    def ProcessElement(c: ProcessContext) {
      val input = c.element()
      val groupKey = JsonUtil.fromJson[TeamWinRate](input.getKey)

      val tableRow = new TableRow()

      taskOfThisJob
        .foreach {
          case "TOP" =>
            tableRow.set("TOP", ChampionUtil.getNameByIndex(groupKey.top))
          case "JUNGLE" =>
            tableRow.set("JUNGLE", ChampionUtil.getNameByIndex(groupKey.jungle))
          case "MIDDLE" =>
            tableRow.set("MIDDLE", ChampionUtil.getNameByIndex(groupKey.middle))
          case "CARRY" =>
            tableRow.set("CARRY", ChampionUtil.getNameByIndex(groupKey.carry))
          case "SUPPORT" =>
            tableRow.set("SUPPORT",
                         ChampionUtil.getNameByIndex(groupKey.support))
        }

      var total = 0
      var win = 0

      input.getValue
        .forEach(stringValue => {
          val ele = JsonUtil.fromJson[TeamWinRate](stringValue)
          total += ele.total
          win += ele.win
        })

      tableRow.set("total", total)
      tableRow.set("win", win)

      c.output(tableRow)
    }
  }

  private final class Translate(taskOfThisJob: List[String]) extends DoFn[TableRow, KV[String, String]] {
    private def getGroupKey(team: TeamWinRate,
      taskOfThisJob: List[String]): String = {
      var top = -1
      var jungle = -1
      var mid = -1
      var carry = -1
      var support = -1

      taskOfThisJob
        .foreach {
          case "TOP"     => top = team.top
          case "JUNGLE"  => jungle = team.jungle
          case "MIDDLE"  => mid = team.middle
          case "CARRY"   => carry = team.carry
          case "SUPPORT" => support = team.support
        }

      val key = new TeamWinRate(top, jungle, mid, carry, support, 0, 0, 0L)
      JsonUtil.toJson(key)
    }

    @ProcessElement
    def ProcessElement(c: ProcessContext) {
        val input = c.element()
        val teamWinRate = TeamWinRateBuilder.getDefaultTeamWin
        teamWinRate.fromBigQueryTableRow(input)
        c.output(KV.of(getGroupKey(teamWinRate, taskOfThisJob), JsonUtil.toJson(teamWinRate)))
    }
  }

  override def execute(jobArgs: JobArgs): Unit = {
    val gcpCredential = CredentialUtil.getCredential

    val taskOfThisJob = jobArgs.combination.split(",").toList
    println(s"We will compute ${taskOfThisJob.mkString(":")} combination")

    val tableSuffix = taskOfThisJob.reduce((a, b) => a + b)

    println(s"Best$tableSuffix")

    val tableSpec =
      new TableReference()
        .setProjectId(config.gcp.projectId)
        .setDatasetId("test_dataset")
        .setTableId(s"Best$tableSuffix")

    val tableFields = new util.ArrayList[TableFieldSchema]
    taskOfThisJob
      .foreach(lane => {
        tableFields.add(
          new TableFieldSchema()
            .setName(lane)
            .setType(LegacySQLTypeName.STRING.name()))
      })

    tableFields.add(
      new TableFieldSchema()
        .setName("total")
        .setType(LegacySQLTypeName.INTEGER.name()))
    tableFields.add(
      new TableFieldSchema()
        .setName("win")
        .setType(LegacySQLTypeName.INTEGER.name()))
    val tableSchema = new TableSchema().setFields(tableFields)

    val championIndexToNameMap = ChampionUtil
      .championStore
      .data
      .values
      .map(champion => champion.key.toInt -> champion.name)
      .toMap

    val query =
      s"""
        |SELECT * FROM `${ConfigUtil.getConfig.gcp.projectId}.test_dataset.best_five`
        |where
        |top != -1 and
        |jungle != -1 and
        |middle != -1 and
        |carry != -1 and
        |support != -1
      """.stripMargin

    val args = jobArgs.dataflowJobArgs.split(" ")
    val options =
      PipelineOptionsFactory.fromArgs(args: _*).create().as(classOf[GcpOptions])
    options.setJobName(
      jobArgs.jobName + LocalDateTime
        .now()
        .toString(Constant.dateTimeFormatter))
    options.setProject(config.gcp.projectId)
    options.setGcpCredential(gcpCredential)
    val p = Pipeline.create(options)

    p
      .apply("Read from bq", BigQueryIO.read().fromQuery(query).usingStandardSql()).setCoder(TableRowJsonCoder.of())
      .apply("translate", ParDo.of(new Translate(taskOfThisJob))).setCoder(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()))
      .apply("GroupByKey", GroupByKey.create[String, String])
      .apply("Aggregation", ParDo.of(new Aggregation(taskOfThisJob, championIndexToNameMap)))
      .apply(
        BigQueryIO
          .writeTableRows()
          .to(tableSpec)
          .withSchema(tableSchema)
          .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
          .withWriteDisposition(WriteDisposition.WRITE_TRUNCATE))

    p.run()
  }
}
