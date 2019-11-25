package apiparser.processor

import apiparser.model.{BaseModel, MatchDto}
import apiparser.util.{CredentialUtil, GcsUtil, JsonUtil}
import scala.collection.mutable

class LaneAgainstMatchData(
    val seasonId: Int = 0,
    val gameId: Long = 0L,
    val gameVersion: String = "default",
    val platformId: String = "default",
    val gameMode: String = "default",
    val mapId: Int = 0,
    val gameType: String = "default",
    val gameDuration: Long = 0L,
    val teamId: Int = 0,
    val win: String = "default",
    val championId: Int = 0,
    val lane: String = "default",
    val role: String = "default"
) extends BaseModel

class FlattenMatchDataByLaneAgainst extends FlattenedMatchDataProcessor {

  override def flattenData(input: MatchDto): List[LaneAgainstMatchData] = {
    val output = new mutable.ListBuffer[LaneAgainstMatchData]()
    val teamReulst = input.teams
      .map(team => (team.teamId, team.win))
      .toMap
    input.participants
      .foreach(party => {
        output.append(
          new LaneAgainstMatchData(
            seasonId = input.seasonId,
            gameId = input.gameId,
            gameVersion = input.gameVersion,
            platformId = input.platformId,
            gameMode = input.gameMode,
            mapId = input.mapId,
            gameType = input.gameType,
            gameDuration = input.gameDuration,
            teamId = party.teamId,
            win = teamReulst.getOrElse(party.teamId, "unknown"),
            championId = party.championId,
            lane = party.timeline.lane,
            role = party.timeline.role
          ))
      })
    output.toList
  }

  override def get(resource: String): List[LaneAgainstMatchData] = {
    val matchData = loadData[Map[String, List[MatchDto]]](resource)

    matchData("matches").iterator
      .flatMap(flattenData)
      .toList
  }

  def getFomGcs(projectId:String, bucket: String, name: String): List[LaneAgainstMatchData] = {
    val gcs = new GcsUtil(projectId, CredentialUtil.getCredential)

    val matchData = JsonUtil.fromJson[Map[String, List[MatchDto]]](gcs.get(bucket, name))

    matchData("matches").iterator
      .flatMap(flattenData)
      .toList
  }
}
