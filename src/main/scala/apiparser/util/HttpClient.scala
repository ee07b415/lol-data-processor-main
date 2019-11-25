package apiparser.util

import scala.io.Source
import java.net.{URL, HttpURLConnection}

object HttpClient {
  def httpGet(path: String): Source = {
    val connection = new URL(path).openConnection
      .asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    val inputStream = connection.getInputStream
    Source.fromInputStream(inputStream)
  }
}
