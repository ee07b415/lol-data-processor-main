package apiparser.model.champion

class info(val attack: Int,
           val defense: Int,
           val magic: Int,
           val difficulty: Int) extends Serializable {

  override def toString = s"info(attack:$attack, defense:$defense, magic:$magic, difficulty:$difficulty)"
}
