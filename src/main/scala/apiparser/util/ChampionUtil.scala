package apiparser.util

import apiparser.model.champion.{ChampionLeagel, champion}
import scala.io.{BufferedSource, Source}

object ChampionUtil {
//  val source: BufferedSource = Source.fromFile("src/main/resources/champions.json")
val source: BufferedSource = Source.fromResource("champions.json")
  val lines: String = try source.mkString
  finally source.close()
  val championStore: ChampionLeagel = JsonUtil.fromJson[ChampionLeagel](lines)

  def getChampionByName(name: String): Option[champion] = {
    championStore.data.get(name)
  }

  def getChampionByIndex(index: String): Option[champion] = {
    championStore.data.values
      .find(_.key.equals(index))
  }

  def getNameByIndex(index: String): String = {
    val inventory = getChampionByIndex(index)
    if (inventory.nonEmpty) inventory.get.name else index
  }

  def getNameByIndex(index: Int): String = {
    val inventory = getChampionByIndex(index.toString)
    if (inventory.nonEmpty) inventory.get.name else index.toString
  }
}
