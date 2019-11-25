package apiparser.processor

import apiparser.model.{BaseModel, MatchDto}
import scala.collection.mutable

class TeamBanMatchData(
    val seasonId: Int,
    val gameVersion: String,
    val platformId: String,
    val gameMode: String,
    val mapId: Int,
    val gameType: String,
    val gameDuration: Long,
    val teamId: Int,
    val win: String,
    val championId: Int
) extends BaseModel

class FlattenMatchDataByTeamBan extends FlattenedMatchDataProcessor {

  override def flattenData(input: MatchDto): List[TeamBanMatchData] = {
    val output = new mutable.ListBuffer[TeamBanMatchData]()
    input.teams
      .foreach(
        team =>
          team.bans
            .foreach(
              ban =>
                output.append(
                  new TeamBanMatchData(
                    input.seasonId,
                    input.gameVersion,
                    input.platformId,
                    input.gameMode,
                    input.mapId,
                    input.gameType,
                    input.gameDuration,
                    team.teamId,
                    team.win,
                    ban.championId
                  ))))
    output.toList
  }

  override def get(resource: String): List[TeamBanMatchData] = {
    val matchData = loadData[Map[String, List[MatchDto]]](resource)

    matchData("matches").iterator
      .flatMap(flattenData)
      .toList
  }
}
