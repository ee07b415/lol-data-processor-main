package apiparser.model.transformed

import apiparser.model.BaseModel

class TeamWinRate(
    val top: Int,
    val jungle: Int,
    val middle: Int,
    val carry: Int,
    val support: Int,
    val total: Int,
    val win: Int,
    val gameId: Long
) extends BaseModel with Serializable {

  def isRegularTeam: Boolean = {
    top != -1 && jungle != -1 && middle != -1 && carry != -1 && support != -1
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[TeamWinRate]

  override def equals(other: Any): Boolean = other match {
    case that: TeamWinRate =>
      (that canEqual this) &&
        top == that.top &&
        jungle == that.jungle &&
        middle == that.middle &&
        carry == that.carry &&
        support == that.support &&
        gameId == that.gameId
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(top, jungle, middle, carry, support, gameId)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString =
    s"TeamWinRate(top=$top, jungle=$jungle, middle=$middle, carry=$carry, support=$support, total=$total, win=$win, gameId=$gameId)"
}

object TeamWinRateBuilder extends Serializable {
  def getDefaultTeamWin:TeamWinRate={
    new TeamWinRate(-1, -1, -1, -1, -1, 0, 0, -1L)
  }
}
