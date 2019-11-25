package apiparser.model.champion

class ChampionLeagel(
    val `type`: String,
    val format: String,
    val version: String,
    val data: Map[String, champion],
) extends Serializable {}
