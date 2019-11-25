package apiparser.model.AcsMatch

object EventsType extends Enumeration {
  type EventsType = Value
  val ITEM_PURCHASED = Value
  val ITEM_SOLD = Value
  val ITEM_UNDO = Value
  val ITEM_DESTROYED = Value

  val SKILL_LEVEL_UP = Value

  val WARD_PLACED = Value
  val WARD_KILL = Value

  val CHAMPION_KILL = Value
  val ELITE_MONSTER_KILL = Value
  val BUILDING_KILL = Value


  val UNKNOWN =Value
}
