package apiparser.job

import apiparser.model.AcsMatch.Match
import apiparser.model.{MatchDto, MatchlistDto, SummonerDTO}
import apiparser.util.{ApiUtil, HttpClient}

class CrawlerJob extends GeneralJob {
  //TODO: refactor with list of api key to speed up on job pulling
  val api_key = config.api_key
  val api_base = "https://na1.api.riotgames.com/lol"
  val cutoff = 100
  val seed_min = 10

  val acs_match_base = "https://acs.leagueoflegends.com/v1/stats/game"

  def getSummonerByName(name: String): SummonerDTO = {
    val source = HttpClient.httpGet(
      s"$api_base/summoner/v4/summoners/by-name/$name?api_key=$api_key")
    val lines: String = try source.mkString
    finally source.close()
    ApiUtil.getSummoner(lines)
  }

  def getMatchListByAccountId(id: String, latestNoOfGame: Int): MatchlistDto = {
    val matchListSource = HttpClient.httpGet(
      s"$api_base/match/v4/matchlists/by-account/$id?api_key=$api_key&endIndex=$latestNoOfGame")
    val matchListString = try matchListSource.mkString
    finally matchListSource.close()
    Thread.sleep(100)
    ApiUtil.getMatchList(matchListString)
  }

  def getMatchByMatchId(id: String): MatchDto = {
    val singleMatchSource =
      HttpClient.httpGet(s"$api_base/match/v4/matches/$id?api_key=$api_key")
    val matchString = try singleMatchSource.mkString
    finally singleMatchSource.close()
    Thread.sleep(100)
    ApiUtil.getMatch(matchString)
  }

  def getAcsMatch(region:String, gameId: String, gameHash: String): Match = {
    val singleMatchSource =
      HttpClient.httpGet(s"$acs_match_base/$gameId/timeline?gameHash=$gameHash")
    val matchString = try singleMatchSource.mkString
    finally singleMatchSource.close()
    ApiUtil.getMatchTimeline(matchString)
  }

  override def whoami(): Unit = ???

  override def execute(jobArgs: JobArgs): Unit = ???
}
