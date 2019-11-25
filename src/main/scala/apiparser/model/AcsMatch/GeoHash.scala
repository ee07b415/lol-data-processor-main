package apiparser.model.AcsMatch

import scala.collection.mutable

case class LocationBlock(hashNumber: Int, x: Int, y: Int, value: Int)

class GeoHash(
    val height: Int = 16000,
    val width: Int = 16000,
    val levelSize: Int = 3 // divide height and width into 2^3
) {
  def generateMap(): Map[Int, LocationBlock] = {
    val geoHashMap = new mutable.HashMap[Int, LocationBlock]()
    val someArray: Array[Array[Int]] = Array.ofDim[Int](
      Math.pow(2, levelSize).intValue(),
      Math.pow(2, levelSize).intValue())

    for (x <- someArray(0).indices) {
      for (y <- someArray.indices) {
        val temp = getHashNumber(x,
                                  y,
                                  0,
                                  someArray.length,
                                  0,
                                  someArray(0).length,
                                  levelSize,
                                  0)
        someArray(y).update(x, temp)
      }
    }

    val reversedMap = someArray.reverse
    for (y <- reversedMap.indices){
      for (x <- reversedMap(0).indices) {
        print(reversedMap(y)(x) + " ");
      }
      println()
    }
    for (x <- reversedMap(0).indices) {
      for (y <- reversedMap.indices) {
        geoHashMap.put(reversedMap(y)(x),
                       LocationBlock(reversedMap(y)(x), x, y, 1))
      }
    }
    geoHashMap.toMap
  }

  def getHashNumber(x: Int,
                     y: Int,
                     heightStart: Int,
                     heightEnd: Int,
                     widthStart: Int,
                     widthEnd: Int,
                     level: Int,
                     initialHash: Int): Int = {
    if (level <= 0) {
      return initialHash / 2
    }

    var hash = initialHash
    var nextHeightStart = 0
    var nextHeightEnd = 0
    var nextWidthStart = 0
    var nextWidthEnd = 0

    if (y < (heightStart + heightEnd) / 2) {
      hash = hash + (0 << level * 2)
      nextHeightStart = heightStart
      nextHeightEnd = (heightStart + heightEnd) / 2
    } else {
      hash = hash + (1 << level * 2)
      nextHeightStart = (heightStart + heightEnd) / 2
      nextHeightEnd = heightEnd
    }

    if (x < (widthStart + widthEnd) / 2) {
      hash = hash + (0 << (level * 2 - 1))
      nextWidthStart = widthStart
      nextWidthEnd = (widthStart + widthEnd) / 2
    } else {
      hash = hash + (1 << (level * 2 - 1))
      nextWidthStart = (widthStart + widthEnd) / 2
      nextWidthEnd = widthEnd
    }

    getHashNumber(x,
                   y,
                   nextHeightStart,
                   nextHeightEnd,
                   nextWidthStart,
                   nextWidthEnd,
                   level - 1,
                   hash)
  }

  def getHashNumber(x: Int, y: Int): Int = {
    getHashNumber(x, y, 0, height, 0, width, levelSize, 0)
  }

  def printBinary(geoHashNumber: Int): String = {
    val sb: mutable.StringBuilder = new mutable.StringBuilder()
    for (i <- 0 until 6) {
      if ((geoHashNumber & (1 << (5 - i))) != 0) {
        sb.append("1")
      } else {
        sb.append("0")
      }
    }
    sb.mkString
  }

  def printLocation(x: Int, y: Int): Unit = {
    val geoHashNumber = getHashNumber(x, y)
    val someArray: Array[Array[Int]] = Array.ofDim[Int](
      Math.pow(2, levelSize).intValue(),
      Math.pow(2, levelSize).intValue())
    val geoMap: Map[Int, LocationBlock] = generateMap()

    val position_x = geoMap(geoHashNumber).x
    val position_y = geoMap(geoHashNumber).y

    someArray(position_y).update(position_x, 1)

    for (y <- someArray.indices) {
      for (x <- someArray(0).indices) {
        print(s"${someArray(y)(x)}  ")
      }
      println()
    }
  }

  def buildHashedMap(): Unit = {

  }
}
