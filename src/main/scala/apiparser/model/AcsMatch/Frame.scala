package apiparser.model.AcsMatch

import apiparser.model.AcsMatch.EventsType.EventsType

class Frame(
  val participantFrames: ParticipantFrames,
  val events: List[Map[String, AnyVal]], // TODO: the schema is not unified... several different event types has different schema
  val timestamp: Int
) {

}
