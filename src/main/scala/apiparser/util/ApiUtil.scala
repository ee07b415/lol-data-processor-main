package apiparser.util

import apiparser.model.AcsMatch.Match
import apiparser.model.{MatchDto, MatchlistDto, SummonerDTO}
import java.io.IOException
import java.time.LocalDateTime
import scala.collection.mutable

object ApiUtil {
  val rate_limit = new mutable.Queue[LocalDateTime]
  val throttle_number = 100

  def rateLimitCheck : Boolean = {
    val currentTS = LocalDateTime.now()
    while( rate_limit.nonEmpty && rate_limit.front.compareTo(currentTS.minusMinutes(2)) < 0){
      rate_limit.dequeue()
    }
    if (rate_limit.size >= throttle_number){
      println(s"we have ${rate_limit.size} events lets wait a little bit time before new quota available")
      false
    } else {
      rate_limit.enqueue(currentTS)
      true
    }
  }

  def getMatchList(input: String) : MatchlistDto =  {
    if (!rateLimitCheck)
      throw new IOException("too fast, Server returned HTTP response code: 429")
    JsonUtil.fromJson[MatchlistDto](input)
  }

  def getSummoner(input: String) : SummonerDTO = {
    if (!rateLimitCheck)
      throw new IOException("too fast, Server returned HTTP response code: 429")
    JsonUtil.fromJson[SummonerDTO](input)
  }

  def getMatch(input: String) : MatchDto = {
    if (!rateLimitCheck)
      throw new IOException("too fast, Server returned HTTP response code: 429")
    JsonUtil.fromJson[MatchDto](input)
  }

  def getMatchTimeline(input: String) : Match = {
    if (!rateLimitCheck)
      throw new IOException("too fast, Server returned HTTP response code: 429")
    JsonUtil.fromJson[Match](input)
  }
}
