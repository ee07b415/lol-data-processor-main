package apiparser.job

object JobName extends Enumeration {
  type JobName = Value
  val WinRate = Value
  val LanePicker = Value
  val GrabMatch = Value
  val SaveFlattenedMatch = Value
  val SeedGenerate = Value
  val MatchAggregator = Value
  val BestFive = Value
  val BestParty = Value
  val Empty = Value
  val ImageProcessing = Value

  val BestFiveBeam = Value
  val BestPartyBeam = Value

  def saveParse(name: String): JobName = {
    if (JobName.values.map(_.toString).contains(name)){
      JobName.withName(name)
    } else {
      JobName.Empty
    }
  }

}
