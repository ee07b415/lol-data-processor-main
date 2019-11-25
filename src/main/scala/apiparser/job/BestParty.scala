package apiparser.job

import apiparser.model.transformed.{TeamWinRate, TeamWinRateBuilder}
import apiparser.util.{BigQueryUtil, ChampionUtil, ConfigUtil, CredentialUtil, FileUtil}
import scala.collection.mutable

/**
  * BestFive.scala is the job store all team champions into Bigquery, this will be a source of truth
  * This job will read from the bigquery and find more configurable pair of the champions.
  *
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=MIDDLE,JUNGLE"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=TOP,JUNGLE"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=TOP,MIDDLE,JUNGLE"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=CARRY,SUPPORT"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=TOP"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=JUNGLE"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=MIDDLE"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=CARRY"
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestParty --combination=SUPPORT"
  *
  * Run with Jar:
  *
  * java -cp dist/apiparser.jar apiparser.Main "--job-name=BestParty" "--combination=TOP,JUNGLE,MIDDLE"
  *
  */
class BestParty extends GeneralJob {
  override def whoami(): Unit = {
    println("This is print from best party job")
  }

  def getGroupKey(team: TeamWinRate,
                  taskOfThisJob: List[String]): TeamWinRate = {
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

    new TeamWinRate(top, jungle, mid, carry, support, 0, 0, 0L)
  }

  def queryBuilder(taskOfThisJob: List[String]): String = {
    val query =
      s"""
        |SELECT sum(total) as total, sum(win) as win, %s %s , -1 as gameId FROM `${ConfigUtil.getConfig.gcp.projectId}.test_dataset.best_five`
        |where
        |top != -1 and
        |middle !=-1 and
        |jungle != -1 and
        |carry != -1 and
        |support != -1
        |group by %s  order by 1 desc, 2 desc limit 100
      """.stripMargin

    val column = taskOfThisJob.map(ele => ele.toLowerCase).mkString(", ")

    val ignoredColumn = List("TOP", "JUNGLE", "MIDDLE", "CARRY", "SUPPORT")
      .filter(!taskOfThisJob.contains(_))
      .map(column => s",-1 as ${column.toLowerCase}")
      .mkString(" ")

    val groupByString = (3 until 3 + taskOfThisJob.size).toList.mkString(", ")

    query.format(column, ignoredColumn, groupByString)
  }

  override def execute(jobArgs: JobArgs): Unit = {
    val taskOfThisJob = jobArgs.combination.split(",").toList
    println(s"We will compute ${taskOfThisJob.mkString(":")} combination")

    val teamLaneList = new mutable.ListBuffer[TeamWinRate]
    val bigquery =
      new BigQueryUtil(config.gcp.projectId, CredentialUtil.getCredential)

    val query = queryBuilder(taskOfThisJob)

    println(query)

    val tableResult = bigquery.readByQuery(query)

    tableResult
      .iterateAll()
      .forEach(row => {
        val teamWinRate = TeamWinRateBuilder.getDefaultTeamWin
        teamWinRate.readFromFieldList(row)
        teamLaneList.append(teamWinRate)
      })

    val data = teamLaneList
      .map(groupedData => {
        val total = groupedData.total
        val win = groupedData.win

        val combinationColumns = taskOfThisJob
          .map {
            case "TOP"    => ChampionUtil.getNameByIndex(groupedData.top)
            case "JUNGLE" => ChampionUtil.getNameByIndex(groupedData.jungle)
            case "MIDDLE" => ChampionUtil.getNameByIndex(groupedData.middle)
            case "CARRY"  => ChampionUtil.getNameByIndex(groupedData.carry)
            case "SUPPORT" => ChampionUtil.getNameByIndex(groupedData.support)
          }
          .mkString(",")
        (combinationColumns, total, win)
      })
      .toList
      .sortBy(_._2)
      .reverse
      .map(ele => s"${ele._1},${ele._2},${ele._3}")

    val bestTopMidJungle: Seq[String] = Seq(
      s"${taskOfThisJob.mkString(",")},total,win") ++ data

    FileUtil.writeFile(s"Best${taskOfThisJob.mkString("")}.csv",
                       bestTopMidJungle,
                       append = false)
  }
}
