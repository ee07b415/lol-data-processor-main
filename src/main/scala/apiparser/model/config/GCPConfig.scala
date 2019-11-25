package apiparser.model.config

class GCPConfig (
  val projectId: String,
  val bitMapbucket: String,
  val matchBucket: String,
  val seedBucket: String,
  val matchPubTopic: String,
  val matchSubTopic: String
) {

}
