package apiparser.util

import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import scala.io.{BufferedSource, Source}

object CredentialUtil {
  val source: BufferedSource = Source.fromResource("credential.json")
  val lines: String = try source.mkString
  finally source.close()

  def getCredential: GoogleCredentials = {
    val serviceAccountStream = new ByteArrayInputStream(lines.getBytes(StandardCharsets.UTF_8))
    ServiceAccountCredentials.fromStream(serviceAccountStream).createScoped("https://www.googleapis.com/auth/cloud-platform")
  }
}
