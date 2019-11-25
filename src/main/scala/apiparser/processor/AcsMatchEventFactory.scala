package apiparser.processor

import apiparser.model.AcsMatch._
import apiparser.util.JsonUtil

object AcsMatchEventFactory {

  def getEvent(map: Map[String, AnyVal]): Events = {
    EventsType.withName(map("type").toString) match {
      case EventsType.ITEM_PURCHASED => JsonUtil.fromMap[ItemPurchased](map)
      case EventsType.ITEM_SOLD => JsonUtil.fromMap[ItemSold](map)
      case EventsType.ITEM_UNDO => JsonUtil.fromMap[ItemUndo](map)
      case EventsType.ITEM_DESTROYED => JsonUtil.fromMap[ItemDestroyed](map)
      case EventsType.SKILL_LEVEL_UP => JsonUtil.fromMap[SkillLevelUp](map)
      case EventsType.WARD_PLACED => JsonUtil.fromMap[WardPlaced](map)
      case EventsType.WARD_KILL => JsonUtil.fromMap[WardKill](map)
      case EventsType.CHAMPION_KILL => JsonUtil.fromMap[ChampionKill](map)
      case EventsType.ELITE_MONSTER_KILL => JsonUtil.fromMap[EliteMonsterKill](map)
      case EventsType.BUILDING_KILL => JsonUtil.fromMap[BuildingKill](map)
      case _ => new Unkown
    }
  }
}
