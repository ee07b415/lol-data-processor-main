package apiparser.model

class MatchlistDto(
    val matches: List[MatchReferenceDto],
    val totalGames: Int,
    val startIndex: Int,
    val endIndex: Int,
) extends Serializable {}
