package apiparser.model.champion

class image(
    val full: String,
    val sprite: String,
    val group: String,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
) extends Serializable {}
