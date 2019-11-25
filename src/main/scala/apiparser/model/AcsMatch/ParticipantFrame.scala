package apiparser.model.AcsMatch

class ParticipantFrame(
  val participantId: Int,
  val position: Position,
  val currentGold: Int,
  val totalGold: Int,
  val level: Int,
  val xp: Int,
  val minionsKilled: Int,
  val jungleMinionsKilled: Int,
  val dominionScore: Int,
  val teamScore: Int
) {
  def getTeam: Int = {
    (participantId - 1) / 5
  }
}
