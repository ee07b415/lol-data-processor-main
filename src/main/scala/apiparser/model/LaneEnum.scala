package apiparser.model

object LaneEnum extends Enumeration {
  type LaneEnum = Value
  val TOP = Value
  val JUNGLE = Value
  val MIDDLE = Value
  val BOTTOM = Value
  val DUO_SUPPORT = Value
  val UNKNOWN =Value

//  def saveParse(name: String): LaneEnum = {
//    if (LaneEnum.values.map(_.toString).contains(name)){
//      LaneEnum.withName(name)
//    } else {
//      name match {
//        case ""
//      }
//    }
//  }
}
