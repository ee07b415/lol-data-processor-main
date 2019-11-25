package apiparser.job

import apiparser.model.config.Config
import apiparser.util.ConfigUtil

abstract class GeneralJob {
  val config: Config = ConfigUtil.getConfig
  def whoami()

  def execute(jobArgs: JobArgs)
}
