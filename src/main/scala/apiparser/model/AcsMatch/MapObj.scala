package apiparser.model.AcsMatch

import java.awt.image.BufferedImage

class MapObj(val image: BufferedImage, val level: Int) {
  val width: Int = image.getWidth()
  val height: Int = image.getHeight()

  //TODO: find the location blocks and the hashed values they belong to
  val laneChangeMap = Map[Int, Set[Int]](
    1 -> Set[Int](2,3),
    2 -> Set[Int](1,3),
    3 -> Set[Int](1,2),
  )

  private val geoHash = new GeoHash(width = width, height = height, levelSize = level)
  val hashedMap: Map[Int, LocationBlock] = geoHash.generateMap()

  def whereAmI(position: Position): LocationBlock ={
    hashedMap(position.geoHashValue(width, height, level))
  }

  def isLaneChange(oldPosition:Position, newPosition:Position): Boolean = {
    val oldBlock = whereAmI(oldPosition)
    val newBlock = whereAmI(newPosition)
    laneChangeMap(oldBlock.value).contains(newBlock.value)
  }
}
