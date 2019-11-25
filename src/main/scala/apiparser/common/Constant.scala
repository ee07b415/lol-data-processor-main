package apiparser.common

import org.joda.time.format.DateTimeFormat


object Constant {
  val dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm")
  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  val gcsPathDateFormatter = DateTimeFormat.forPattern("yyyy/MM/dd")
}
