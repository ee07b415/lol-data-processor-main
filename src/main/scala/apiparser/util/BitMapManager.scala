package apiparser.util

import apiparser.model.config.Config
import org.roaringbitmap.longlong.Roaring64NavigableMap

trait BitMapManager {
  def get:Roaring64NavigableMap
  def set(matchBitMap: Roaring64NavigableMap)
}

class GCPBitMapManager(config: Config) extends BitMapManager {
  private val projectId = config.gcp.projectId
  val gcs = new GcsUtil(projectId, CredentialUtil.getCredential)

  override def get: Roaring64NavigableMap = {
    gcs.getMatchBitMap(config.gcp.bitMapbucket, config.file_name.bitMap)
  }

  override def set(matchBitMap: Roaring64NavigableMap): Unit = {
    gcs.saveMatchBitMap(matchBitMap, config.gcp.bitMapbucket, config.file_name.bitMap)
  }
}

class LocalBitMapManager(config: Config) extends BitMapManager {
  override def get: Roaring64NavigableMap = {
    FileUtil.getLocalMatchListBitMap(s"${config.local.bitMapfolder}/${config.file_name.bitMap}")
  }

  override def set(matchBitMap: Roaring64NavigableMap): Unit = {
    FileUtil.setLocalMatchListBitMap(config.local.bitMapfolder + config.file_name.bitMap, matchBitMap)
  }
}

object BitMapFactory {
  def getBitMapManager(config: Config): BitMapManager ={
    if (config.local_mode){
      new LocalBitMapManager(config)
    } else {
      new GCPBitMapManager(config)
    }
  }
}
