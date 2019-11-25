package apiparser.processor

import apiparser.model.AcsMatch.{BuildingKill, ChampionKill, EliteMonsterKill, Events, EventsType, Frame, ParticipantFrames}
import apiparser.model.{BaseModel, MatchDto}
import scala.collection.mutable

trait FlattenedAcsMatchDataProcessor extends BaseDataProcessor {
  def flattenData(input: MatchDto): List[BaseModel]

  def get(resource: String): List[BaseModel]
}

case class AcsMatchEvent(
  participantFrames: ParticipantFrames,
  timestamp: Int,
  events: Events
) {
  def isItemEvent: Boolean = {
    EventsType.withName(events.`type`) match {
      case EventsType.ITEM_PURCHASED => true
      case EventsType.ITEM_SOLD => true
      case EventsType.ITEM_UNDO => true
      case EventsType.ITEM_DESTROYED => true
      case _ => false
    }
  }

  def isWardEvent: Boolean = {
    EventsType.withName(events.`type`) match {
      case EventsType.WARD_PLACED => true
      case EventsType.WARD_KILL => true
      case _ => false
    }
  }
}



class AcsMatchDataProcessor (val input: List[AcsMatchEvent]) {

  def straDiffImpacIndex(straDiff: Int): Double = {
    (straDiff * straDiff) / (6000.0 * 6000.0)
  }

  def goldImpactIndex(curGold: Int): Double = {
    (curGold - 3000) * (curGold - 70000) / (33500.0 * 33500.0)
  }

  // every 1000 damage is 250 strategy income
  val damageEffortIndex = 250 / 1000.0
  val damageDiffEffortIndex = 200 / 1000.0
  val wardPlaceIndex = 67.5
  val wardKillIndex = 15
  val itemEventIndex = 200
  val laneChangeStrategyIncome = 100
  val championKillBonus = 300
  val eliteKillBonus = 400
  val largeEliteKillBonus = 1700
  val buildingKillBonus = 500
  val crystalKillBonus = 560

  class TeamTime(
    val team: Int,
    val timestamp: Int,
    val memberIndex: Int
  )

  val timestampList = new mutable.ListBuffer[Int]

  // effort
  val csTimeDis = new mutable.HashMap[TeamTime, Int]()
  val wardPlacedDis = new mutable.HashMap[TeamTime, Int]()
  val wardKillDis = new mutable.HashMap[TeamTime, Int]()
  val ItemEventDis = new mutable.HashMap[TeamTime, Int]()
  val laneChangeEventDis = new mutable.HashMap[TeamTime, Int]()

  // secondary effort, effort income
  val championKillEventDis = new mutable.HashMap[TeamTime, Int]()
  val eliteKillEventDis = new mutable.HashMap[TeamTime, Int]()
  val largeEliteKillEventDis = new mutable.HashMap[TeamTime, Int]()

  // effort income
  val buildingKillEventDis = new mutable.HashMap[TeamTime, Int]()
  val crystalKillEventDis = new mutable.HashMap[TeamTime, Int]()


  /**
    *
    * @param straDiff: strategy income diff
    * @param curGold: cur gold for the team who's total gold is fell behind
    * @return
    */
  def effortPunishIndex(straDiff: Int, curGold: Int): Double= {
    straDiffImpacIndex(straDiff) * goldImpactIndex(curGold)
  }

  def initial(): Unit ={
    timestampList.appendAll(input.map(_.timestamp).sorted)

    getWardEvents
      .foreach(event => {
        val teamEventKey = new TeamTime(event.events.getTeam, event.timestamp, event.events.getEventer)
        EventsType.withName(event.events.`type`) match {
          case EventsType.WARD_PLACED => wardPlacedDis.put(teamEventKey, event.events.timestamp)
          case EventsType.WARD_KILL => wardKillDis.put(teamEventKey, event.events.timestamp)
          case _ =>
        }
      })

    getItemEvents
      .foreach(event => {
        val teamEventKey = new TeamTime(event.events.getTeam, event.timestamp, event.events.getEventer)
        ItemEventDis.put(teamEventKey, event.events.timestamp)
      })

    input
      .filter(event => event.events.isInstanceOf[ChampionKill])
      .foreach(event => {
        val teamEventKey = new TeamTime(event.events.getTeam, event.timestamp, event.events.getEventer)
        championKillEventDis.put(teamEventKey, event.events.timestamp)
      })

    input
      .filter(event => event.events.isInstanceOf[EliteMonsterKill])
      .foreach(event => {
        val teamEventKey = new TeamTime(event.events.getTeam, event.timestamp, event.events.getEventer)
        eliteKillEventDis.put(teamEventKey, event.events.timestamp)
      })

    input
      .filter(event => event.events.isInstanceOf[BuildingKill])
      .foreach(event => {
        val teamEventKey = new TeamTime(event.events.getTeam, event.timestamp, event.events.getEventer)
        buildingKillEventDis.put(teamEventKey, event.events.timestamp)
      })

  }

  def apply(input: List[Frame]): AcsMatchDataProcessor = {
    new AcsMatchDataProcessor(
      input
        .flatMap(e => {
          e.events
            .map(mapele =>
              AcsMatchEvent(e.participantFrames, e.timestamp, AcsMatchEventFactory.getEvent(mapele))
            )
        })
    )
  }



  def getItemEvents: List[AcsMatchEvent]={
    input
      .filter(e => e.isItemEvent)
  }

  def getWardEvents: List[AcsMatchEvent]={
    input
      .filter(e => e.isWardEvent)
  }
}
