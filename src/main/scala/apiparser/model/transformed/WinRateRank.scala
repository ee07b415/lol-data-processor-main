package apiparser.model.transformed

import apiparser.model.BaseModel

class WinRateRank(
    val champion: String = "default",
    val win: Int = 0,
    val fail: Int = 0,
    val winRate: Double = 0,
    val failRate: Double = 0,
) extends BaseModel {

  override def toString =
    s"WinRateRank($champion, $win, $fail, $winRate, $failRate)"
}
