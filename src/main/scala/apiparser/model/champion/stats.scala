package apiparser.model.champion

class stats(
    val hp: Double,
    val hpperlevel: Double,
    val mp: Double,
    val mpperlevel: Double,
    val movespeed: Double,
    val armor: Double,
    val armorperlevel: Double,
    val spellblock: Double,
    val spellblockperlevel: Double,
    val attackrange: Double,
    val hpregen: Double,
    val hpregenperlevel: Double,
    val mpregen: Double,
    val mpregenperlevel: Double,
    val crit: Double,
    val critperlevel: Double,
    val attackdamage: Double,
    val attackdamageperlevel: Double,
    val attackspeedoffset: Double,
    val attackspeedperlevel: Double,
) extends Serializable {

}
