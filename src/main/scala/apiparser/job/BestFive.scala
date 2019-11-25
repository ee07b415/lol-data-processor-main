package apiparser.job

import apiparser.model.transformed.{Team, TeamWinRate}
import apiparser.processor.FlattenMatchDataByTeamLane
import apiparser.util.{BigQueryUtil, CredentialUtil}

/**
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=BestFive --future-config='justise'"
  *
  * Run with Jar:
  *
  * java -cp dist/apiparser.jar apiparser.Main "--job-name=BestFive" "--future-config=justise come to all"
  *
  */
class BestFive extends GeneralJob {
  override def whoami(): Unit = {
    println("This is print from best five job")
  }

  override def execute(jobArgs: JobArgs): Unit = {
    val bigquery =
      new BigQueryUtil(config.gcp.projectId, CredentialUtil.getCredential)
    val dataProcessor = new FlattenMatchDataByTeamLane()
    val data = dataProcessor.get("match_local.json")
    val flatData = data
      .flatMap(game => {
        val teamOne = new Team(
          game.teamOneTopChampionId,
          game.teamOneJungleChampionId,
          game.teamOneMidChampionId,
          game.teamOneCarryChampionId,
          game.teamOneSupportChampionId,
          game.gameId
        )

        val teamTwo = new Team(
          game.teamTwoTopChampionId,
          game.teamTwoJungleChampionId,
          game.teamTwoMidChampionId,
          game.teamTwoCarryChampionId,
          game.teamTwoSupportChampionId,
          game.gameId
        )
        List(
          (teamOne, (1, if (game.winTeamId == 100) 1 else 0)),
          (teamTwo, (1, if (game.winTeamId == 200) 1 else 0))
        )
      })
      .groupBy(_._1)
      .map(groupedResult => {
        val team = groupedResult._1
        val total = groupedResult._2.map(_._2._1).sum
        val win = groupedResult._2.map(_._2._2).sum
        new TeamWinRate(team.top,
                        team.jungle,
                        team.middle,
                        team.carry,
                        team.support,
                        total,
                        win,
                        team.gameId)
      })

    val rows = flatData.map(x => x.buildTableRow)

//    val schema = flatData.head.makeTableSchema
//    bigquery.createTable("test_dataset", "best_five", schema)
//    println("table create success!")

    bigquery.write("test_dataset", "best_five", rows.toList)
  }
}
