package apiparser.util

import apiparser.model.config.Config
import scala.io.{BufferedSource, Source}

object ConfigUtil {
  val source: BufferedSource = Source.fromResource("config-prod.json")
  val lines: String = try source.mkString
  finally source.close()

  def getConfig: Config = {
    JsonUtil.fromJson[Config](lines)
  }
}
