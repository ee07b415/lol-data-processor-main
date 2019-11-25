package apiparser.model.AcsMatch

import apiparser.model.BaseModel

class Position(
  val x: Int,
  val y: Int
) extends BaseModel {
  def geoHashValue(mapWidth:Int, mapHeight:Int, mapSmeshLevel:Int): Int = {
    new GeoHash(width = mapWidth, height = mapHeight, levelSize = mapSmeshLevel).getHashNumber(x, y)
  }
}
