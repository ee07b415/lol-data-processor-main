package apiparser.model.transformed

import apiparser.model.BaseModel

class Team(
    val top: Int,
    val jungle: Int,
    val middle: Int,
    val carry: Int,
    val support: Int,
    val gameId: Long
) extends BaseModel with Serializable {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Team]

  override def equals(other: Any): Boolean = other match {
    case that: Team =>
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
}
