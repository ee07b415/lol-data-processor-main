package apiparser.model

class MatchDto(
    val seasonId: Int,
    val queueId: Int,
    val gameId: Long,
    val participantIdentities: List[ParticipantIdentityDto],
    val gameVersion: String,
    val platformId: String,
    val gameMode: String,
    val mapId: Int,
    val gameType: String,
    val teams: List[TeamStatsDto],
    val participants: List[ParticipantDto],
    val gameDuration: Long,
    val gameCreation: Long
) extends Serializable {
  def isMatchGame: Boolean = {
    gameMode.equals("CLASSIC") && mapId == 11 && gameType.equals("MATCHED_GAME")
  }

  def getGameVersionMain: String = {
    val gameVersionArray = gameVersion.split("\\.")
    gameVersionArray.splitAt(2)._1.mkString(".")
  }
}
