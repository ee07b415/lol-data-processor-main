package apiparser.processor

import apiparser.util.JsonUtil
import scala.io.Source

abstract class BaseDataProcessor {
  def loadData[T](resource: String)(implicit m: Manifest[T]): T = {
    val source = Source.fromResource(resource)
    val lines = try source.mkString
    finally source.close()

    JsonUtil.fromJson[T](lines)
  }
}
