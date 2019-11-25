package apiparser.model.AcsMatch

import apiparser.model.BaseModel

trait Events extends BaseModel {
  val `type`: String = ""
  val timestamp: Int = -1

  def getEventer: Int

  def getTeam: Int = {
    (getEventer - 1) / 5
  }
}

class Unkown extends Events {
  override val `type`: String = EventsType.UNKNOWN.toString

  override def getEventer: Int = -1
}

trait ItemEvent extends Events {
  val participantId: Int = -1
  val itemId: Int = -1

  override def getEventer: Int = participantId
}

class ItemPurchased extends ItemEvent
class ItemSold extends ItemEvent
class ItemDestroyed extends ItemEvent
class ItemUndo extends Events {
  val participantId: Int = -1
  val afterId: Int = -1
  val beforeId: Int = -1

  override def getEventer: Int = participantId
}

class SkillLevelUp extends Events {
    val participantId: Int = -1
    val skillSlot: Int = -1
    val levelUpType: String = ""

  override def getEventer: Int = participantId
}

trait WardEvent extends Events {
  val wardType: String = ""
}

trait KillEvents extends Events {
  val killerId: Int = -1
  override def getEventer: Int = killerId
}

class WardPlaced extends WardEvent {
  val creatorId: Int = -1
  override def getEventer: Int = creatorId
}

class WardKill extends WardEvent {
  val killerId: Int = -1
  override def getEventer: Int = killerId
}

trait EnemyKill extends KillEvents {
  val position: Position = new Position(-1, -1)
}

class ChampionKill extends EnemyKill {
  val victimId: Int = -1
  val assistingParticipantIds: List[Int] = List[Int]()
}

class EliteMonsterKill extends EnemyKill{
  val monsterType: String = ""
  val monsterSubType: String = ""
}

class BuildingKill extends KillEvents {
  val assistingParticipantIds: List[Int] = List[Int]()
  val teamId: Int = -1
  val buildingType: String = ""
  val laneType: String = ""
  val towerType: String = ""
}
