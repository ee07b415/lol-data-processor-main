package apiparser.job

import apiparser.processor.FlattenMatchDataByLaneAgainst
import apiparser.util.ChampionUtil
import scala.collection.mutable

/**
 * Run in pants:
 * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=LanePicker --future-config='justise come to all'"
 *
 * Run with Jar:
 *
 * java -cp dist/apiparser.jar apiparser.Main "--job-name=LanePicker" "--future-config=justise come to all"
 */
@Deprecated
class LaneChampionPickerSuggestion extends GeneralJob {
  def getData: List[(Int, Seq[(Int, Double, Int)])] = {
    //(picked champion, enemy champion, result)
    var dataList = new mutable.ListBuffer[(Int, Int, String)]()
    val flattenedData = new FlattenMatchDataByLaneAgainst()
    flattenedData
      .get("match_02.json")
      .groupBy(data => (data.gameId, data.lane))
      .toSeq
      .foreach(ele => {

        val teamChampionMap =
          ele._2.map(lane => (lane.teamId, lane.championId)).groupBy(_._1)
        ele._2
          .foreach(lane => {
            teamChampionMap
              .filter(!_._1.equals(lane.teamId))
              .foreach(enemyTeam => {
                enemyTeam._2
                  .map(_._2)
                  .foreach(champion => {
                    dataList.append(
                      (
                        lane.championId, // our champion
                        champion, // enemy champion
                        lane.win // result
                      )
                    )
                  })
              })
          })
      })

    dataList
      .groupBy(_._1) // grouped by picked
      .toSeq
      .map(groupedEle => {
        val championId = groupedEle._1
        //List[(enemy championId, win rate, total matches)]
        val enemyRateList = groupedEle._2
          .groupBy(_._2) // grouped by enemy
          .toSeq
          .map(enemyList => {
            val total = if (enemyList._2.isEmpty) 1 else enemyList._2.size
            val win = enemyList._2.count(_._3.equals("Win"))
            (enemyList._1, win / total.toDouble, total)
          })
          .sortBy(_._2) //enemy win rate high to low
        (championId, enemyRateList)
      })
      .toList
  }

  override def whoami(): Unit = {
    println("print from lanepicker job")
  }

  override def execute(jobArgs: JobArgs): Unit = {
    // Another bad naming job, this gives the data of selected champion and the
    // win rate list from lowest to highest enemy, which means if someone choose
    // this champion, you should pick up the first one from the list (you enemy has
    // the lowest win rate against you)
    val data = getData
    data
      .splitAt(3)
      ._1
      .foreach(ele => {
        println(s"${ChampionUtil.getNameByIndex(ele._1.toString)} stat:")
        ele._2
          .splitAt(3) //TOP three
          ._1
          .foreach(rateMap => {
            println(
              f"win ${ChampionUtil.getNameByIndex(rateMap._1.toString)} " +
                f"at rate ${rateMap._2 * 100}%2.2f%% in ${rateMap._3} games")
          })
      })
  }
}
