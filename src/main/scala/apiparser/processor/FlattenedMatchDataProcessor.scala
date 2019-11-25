package apiparser.processor

import apiparser.model.{BaseModel, MatchDto}

trait FlattenedMatchDataProcessor extends BaseDataProcessor {
  def flattenData(input: MatchDto): List[BaseModel]

  def get(resource: String): List[BaseModel]
}
