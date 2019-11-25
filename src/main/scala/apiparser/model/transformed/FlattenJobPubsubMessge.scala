package apiparser.model.transformed

import apiparser.common.Constant
import apiparser.model.BaseModel
import org.joda.time.LocalDate

class FlattenJobPubsubMessge(
    val bucket: String,
    val date: String,
    val file: String
) extends BaseModel {
  def getDatePath: String = {
    LocalDate.parse(date).toString(Constant.gcsPathDateFormatter)
  }

  def getDateFilePath: String = {
    s"$getDatePath/$file"
  }

  def getFilePath: String = {
    s"gs://$bucket/$getDateFilePath"
  }
}
