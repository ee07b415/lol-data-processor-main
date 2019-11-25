package apiparser.model

class TeamStatsDto(val firstDragon: Boolean,
                   val firstInhibitor: Boolean,
                   val bans: List[TeamBansDto],
                   val baronKills: Int,
                   val firstRiftHerald: Boolean,
                   val firstBaron: Boolean,
                   val riftHeraldKills: Int,
                   val firstBlood: Boolean,
                   val teamId: Int,
                   val firstTower: Boolean,
                   val vilemawKills: Int,
                   val inhibitorKills: Int,
                   val towerKills: Int,
                   val dominionVictoryScore: Int,
                   val win: String,
                   val dragonKills: Int) extends Serializable
