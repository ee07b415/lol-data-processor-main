package apiparser.processor

import apiparser.model.champion.ChampionFilter
import apiparser.model.{BaseModel, MatchDto, ParticipantDto}
import apiparser.util.{CredentialUtil, GcsUtil, JsonUtil}
import scala.collection.mutable
import tutorial.TeamResultStatus
import tutorial.TeamResultStatus.Person.PhoneType

class TeamLaneResultStatusData(
    val seasonId: Int = 0,
    val gameId: Long = 0L,
    val gameVersion: String = "default",
    val platformId: String = "default",
    val gameMode: String = "default",
    val mapId: Int = 0,
    val gameType: String = "default",
    val gameDuration: Long = 0L,
    val winTopChampionId: Int = 0,
    val winJungleChampionId: Int = 0,
    val winMidChampionId: Int = 0,
    val winCarryChampionId: Int = 0,
    val winSupportChampionId: Int = 0,
    val failTopChampionId: Int = 0,
    val failJungleChampionId: Int = 0,
    val failMidChampionId: Int = 0,
    val failCarryChampionId: Int = 0,
    val failSupportChampionId: Int = 0,
) extends BaseModel with Serializable

class FlattenMatchDataByResultTeamLaneStatus extends FlattenedMatchDataProcessor {

  /**
    * Assign the champion to lane/role based on the lane and role parameter in participant timeline
    * @param input
    * @return
    */
  def findTeamLaneRole(input: List[ParticipantDto]): Map[String, Int] = {
    input
      .map(
        player => {
          player.timeline.lane match {
            case "TOP"    => ("TOP", player.championId)
            case "MIDDLE" => ("MIDDLE", player.championId)
            case "JUNGLE" => ("JUNGLE", player.championId)
            case _ =>
              player.timeline.role match {
                case "DUO_SUPPORT" => ("SUPPORT", player.championId)
                case "DUO_CARRY"   => ("CARRY", player.championId)
                case "DUO" =>
                  if (ChampionFilter.inferred_support.contains(
                        player.championId)) {
                    ("SUPPORT", player.championId)
                  } else {
                    ("CARRY", player.championId)
                  }
                case "SOLO" =>
                  if (ChampionFilter.inferred_support.contains(
                        player.championId)) {
                    ("SUPPORT", player.championId)
                  } else {
                    ("CARRY", player.championId)
                  }
              }
          }
        })
      .toMap
  }

  /**
    * Riot did really bad statistic data on team, a lot of lane and role are unmarked or bad mark,
    * we will only do a really simple modification here to reform the team, we will do a first round
    * team lane matching, this will result in the duplicate record overwrite be whoever come later,
    * for example, some match do have two TOP but no MIDDLE, the result from findTeamLaneRole method
    * will have no MIDDLE, and TOP will be the second participant mark as TOP, there is no way we
    * can recover the match lane assignment, so we will just assign the first TOP to MIDDLE, this
    * logic apply for all lane. If two or more lane doesn't have any match champion, it means a team
    * game or not match game, assign -1 as championId so we can filter those matches later.
    * @param input
    * @param teamId
    * @return
    */
  def getTeamMap(input: MatchDto, teamId: Int): Map[String, Int] = {
    val teamRawData =
      input.participants.filter(player => player.teamId == teamId)
    val teamLaneMatchFirstRound = findTeamLaneRole(teamRawData)

    //find the missing champion
    val teamChampionUnassigned = teamRawData
      .map(player => player.championId)
      .filter(championId => {
        !teamLaneMatchFirstRound.values.toList.contains(championId)
      })
    //we only do the correction for 1 lane miss match, if more than 2 lane can't match, just give -1
    val unassigned =
      if (teamChampionUnassigned.size == 1) teamChampionUnassigned.head else -1

    Map[String, Int](
      "TOP" -> teamLaneMatchFirstRound.getOrElse("TOP", unassigned),
      "JUNGLE" -> teamLaneMatchFirstRound.getOrElse("JUNGLE", unassigned),
      "MIDDLE" -> teamLaneMatchFirstRound.getOrElse("MIDDLE", unassigned),
      "CARRY" -> teamLaneMatchFirstRound.getOrElse("CARRY", unassigned),
      "SUPPORT" -> teamLaneMatchFirstRound.getOrElse("SUPPORT", unassigned)
    )
  }

  override def flattenData(input: MatchDto): List[TeamLaneMatchData] = {
    val output = new mutable.ListBuffer[TeamLaneMatchData]()
    val teamResult = input.teams
      .map(team => (team.teamId, team.win))
      .toMap
    val winTeamId =
      input.teams.filter(team => team.win.equals("Win")).map(_.teamId).head

    val teamOne = getTeamMap(input, 100)
    val teamTwo = getTeamMap(input, 200)

    output.append(
      new TeamLaneMatchData(
        seasonId = input.seasonId,
        gameId = input.gameId,
        gameVersion = input.gameVersion,
        platformId = input.platformId,
        gameMode = input.gameMode,
        mapId = input.mapId,
        gameType = input.gameType,
        gameDuration = input.gameDuration,
        winTeamId = winTeamId,
        teamOneTopChampionId = teamOne("TOP"),
        teamOneJungleChampionId = teamOne("JUNGLE"),
        teamOneMidChampionId = teamOne("MIDDLE"),
        teamOneCarryChampionId = teamOne("CARRY"),
        teamOneSupportChampionId = teamOne("SUPPORT"),
        teamTwoTopChampionId = teamTwo("TOP"),
        teamTwoJungleChampionId = teamTwo("JUNGLE"),
        teamTwoMidChampionId = teamTwo("MIDDLE"),
        teamTwoCarryChampionId = teamTwo("CARRY"),
        teamTwoSupportChampionId = teamTwo("SUPPORT"),
      )
    )
    output.toList
  }

  override def get(resource: String): List[TeamLaneMatchData] = {
    val matchData = loadData[Map[String, List[MatchDto]]](resource)

    //TODO: testing on protobuf at this time
    val person = TeamResultStatus.AddressBook.getDefaultInstance

    matchData("matches").iterator
      .filter(_.isMatchGame)
      .flatMap(flattenData)
      .toList
  }

  def getFomGcs(projectId: String,
                bucket: String,
                name: String): List[TeamLaneMatchData] = {
    val gcs = new GcsUtil(projectId, CredentialUtil.getCredential)

    val matchData =
      JsonUtil.fromJson[Map[String, List[MatchDto]]](gcs.get(bucket, name))

    matchData("matches").iterator
      .flatMap(flattenData)
      .toList
  }
}
