package apiparser.model

class SummonerDTO(
  val profileIconId:Int,
  val name:String,
  val puuid:String,
  val summonerLevel:Long,
  val revisionDate:Long,
  val id:String,
  val accountId:String,
) extends Serializable {

}
