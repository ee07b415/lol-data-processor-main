package apiparser.model.transformed

import apiparser.model.BaseModel

class BanWinRateRank(
    val champion: String = "default",
    val win: Int = 0,
    val fail: Int = 0,
    val winRate: Double = 0,
    val failRate: Double = 0,
) extends BaseModel {

  override def toString =
    s"ChampionBanWinRate($champion, $win, $fail, $winRate, $failRate)"


  def canEqual(other: Any): Boolean = other.isInstanceOf[BanWinRateRank]

  override def equals(other: Any): Boolean = other match {
    case that: BanWinRateRank =>
      (that canEqual this) &&
        champion == that.champion &&
        win == that.win &&
        fail == that.fail &&
        winRate == that.winRate &&
        failRate == that.failRate
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(champion, win, fail, winRate, failRate)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
