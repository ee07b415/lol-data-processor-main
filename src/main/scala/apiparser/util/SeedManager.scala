package apiparser.util

import apiparser.model.config.Config

trait SeedManager {
  def get:List[String]
  def set(matchIdList: List[String])
}

class GCPSeedManager(config: Config) extends SeedManager {
  private val projectId = config.gcp.projectId
  val gcs = new GcsUtil(projectId, CredentialUtil.getCredential)

  override def get: List[String] = {
    gcs.get(config.gcp.seedBucket, config.file_name.seed).split("\n").toList
  }

  override def set(matchIdList: List[String]): Unit = {
    gcs.save(config.gcp.seedBucket, config.file_name.seed, matchIdList.mkString("\n"))
  }
}

class LocalSeedManager(config: Config) extends SeedManager {
  override def get: List[String] = {
    FileUtil.getFile[List[String]](config.local.seedfolder + config.file_name.seed)
  }

  override def set(matchIdList: List[String]): Unit = {
    FileUtil.writeFile(config.file_name.seed, matchIdList, append = false)
  }
}

object SeedManagerFactory {
  def getSeedManager(config: Config): SeedManager = {
    if (config.local_mode){
      new LocalSeedManager(config)
    } else {
      new GCPSeedManager(config)
    }
  }
}
