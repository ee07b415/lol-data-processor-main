package apiparser.job

import apiparser.model.transformed.BanWinRateRank
import apiparser.processor.FlattenMatchDataByTeamBan
import apiparser.util.{BigQueryUtil, ChampionUtil, ConfigUtil, CredentialUtil}

/**
 * A bad naming file, this file will find out the the win rate if you ban the specific champion not choosing
 * Run in pants:
 * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=WinRate --future-config='justise come to all'"
 *
 * Run with Jar:
 *
 * java -cp dist/apiparser.jar apiparser.Main "--job-name=WinRate" "--future-config=justise come to all"
 */
@Deprecated
class WinRateRankTopN() extends GeneralJob {
  val n = 20
  def getData: List[BanWinRateRank] = {
    val flattenedData = new FlattenMatchDataByTeamBan()

    val inter = flattenedData
      .get("match_02.json")
      .groupBy(_.championId)
      .toSeq
      .filter(ele => ele._1 != -1)

    inter
      .map(ele => {
        val name = ChampionUtil.getNameByIndex(ele._1.toString)
        val wins = ele._2
          .groupBy(game => game.win)
          .toSeq
          .map(groupedGame => (groupedGame._1, groupedGame._2.size))
          .toMap[String, Int]

        new BanWinRateRank(
          name,
          wins.getOrElse("Win", 0),
          wins.getOrElse("Fail", 0),
          wins.getOrElse("Win", 0) / ele._2.size.toDouble,
          wins.getOrElse("Fail", 0) / ele._2.size.toDouble)
      })
      .sortBy(ele => ele.win + ele.fail)
      .reverse
//      .splitAt(n)
//      ._1
      .sortBy(_.winRate)
      .reverse
      .toList
  }

  override def whoami(): Unit = {
    println("print from winrate job")
  }

  override def execute(jobArgs: JobArgs): Unit = {
//        getData
//          .foreach(stat => {
//            println(
//              f"Champion:${stat.champion}%-10s banned ${stat.win + stat.fail}%3d times, win ${stat.win}%3d games " +
//                f"with ${stat.winRate * 100}%2.2f%% win rate")
//          })
    val projectId = ConfigUtil.getConfig.gcp.projectId
    val bigquery = new BigQueryUtil(projectId, CredentialUtil.getCredential)

    val data = getData
    val schema = data.head.makeTableSchema

//    bigquery.createTable("test_dataset", "ban_win_rate", schema)
//
//    println("table create success!")
//
//    val rows = data.map(x => x.buildTableRow)
//
//    bigquery.write("test_dataset", "ban_win_rate", rows)
//
//    println(s"${rows.size} of rows appended")
  }
}
