package apiparser.util

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.{Blob, BlobId, BlobInfo, StorageOptions}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}
import java.nio.charset.StandardCharsets
import org.roaringbitmap.longlong.Roaring64NavigableMap
import scala.collection.mutable
import scala.util.matching.Regex

class GcsUtil(val projectId: String, val credentials: GoogleCredentials) {
  val storage = StorageOptions.newBuilder()
    .setProjectId(projectId)
    .setCredentials(credentials)
    .build
    .getService

  def get(bucket:String, name:String): String ={
    new String(storage.get(bucket, name).getContent(), StandardCharsets.UTF_8)
  }

  def save(bucket: String, name: String, content: String) : Unit ={
    val blobId = BlobId.of(bucket, name)
    val blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build
    val blob = storage.create(blobInfo, content.getBytes(StandardCharsets.UTF_8))
  }

  def expand(bucket: String, pattern: Regex = null): List[String] = {
    val paths = new mutable.ListBuffer[String]
    if (pattern != null){
      val regexPattern = pattern
      storage.list(bucket).iterateAll()
        .forEach(blob => {
          blob.getName match {
            case regexPattern(_*) => paths.append(blob.getName)
            case _ =>
          }
        })
    } else {
      storage.list(bucket).iterateAll()
        .forEach(blob => {
          paths.append(blob.getName)
        })
    }

    paths.toList
  }

  /**
   * Example code for cloud based bitmap:
   * val projectId = ConfigUtil.getConfig.gcp.projectId
   * val gcs = new GcsUtil(projectId, CredentialUtil.getCredential)
   * val rb_ver = gcs.getMatchBitMap("match_bitmap", "matchList.bin")
   * println(rb_ver.contains(3151160023L))
   * println(rb_ver.contains(1))
   * println(rb_ver.contains(2))
   * gcs.saveMatchBitMap(rb_ver, "match_bitmap", "matchList.bin")
   *
   * We will try to use the bitmap holding all the match id grab from riot api
   *
   * @param localMatchListBitMap
   * @param bucket
   * @param name
   */
  def saveMatchBitMap(localMatchListBitMap:Roaring64NavigableMap, bucket: String, name: String): Unit = {
    val byteStream = new ByteArrayOutputStream()
    val out = new DataOutputStream(byteStream)
    localMatchListBitMap.serialize(out)
    val blobId = BlobId.of(bucket, name)
    val blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build
    storage.create(blobInfo, byteStream.toByteArray)
  }

  def getMatchBitMap(bucket:String, name:String): Roaring64NavigableMap ={
    val localMatchListBitMap = new Roaring64NavigableMap
    val byteStream = new ByteArrayInputStream(storage.get(bucket, name).getContent())
    val in = new DataInputStream(byteStream)
    localMatchListBitMap.deserialize(in)
    localMatchListBitMap
  }
}
