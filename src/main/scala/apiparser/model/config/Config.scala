package apiparser.model.config

class Config(
  val api_key: String,
  val seed: List[String],
  val gcp: GCPConfig,
  val local_mode: Boolean,
  val file_name: NameMap,
  val local: LocalConfig
) {

}
