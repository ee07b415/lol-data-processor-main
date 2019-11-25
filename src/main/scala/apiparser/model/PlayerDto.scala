package apiparser.model

class PlayerDto(
    val currentPlatformId: String,
    val summonerName: String,
    val matchHistoryUri: String,
    val platformId: String,
    val currentAccountId: String,
    val profileIcon: Int,
    val summonerId: String,
    val accountId: String,
) extends Serializable
