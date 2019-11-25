package apiparser.util

import java.io.{BufferedWriter, DataInputStream, DataOutputStream, File, FileInputStream, FileOutputStream, FileWriter}
import org.roaringbitmap.longlong.Roaring64NavigableMap
import scala.io.{BufferedSource, Source}

object FileUtil {
  def writeFile(filename: String, s: String): Unit = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file, false))
    bw.write(s)
    bw.close()
  }

  def writeFile(filename: String, lines: Seq[String], append: Boolean = true): Unit = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file, append))

    for (line <- lines.take(1)) {
      bw.write(line)
    }

    for (line <- lines.takeRight(lines.size - 1)) {
      bw.newLine()
      bw.write(line)
    }

    bw.close()
  }

  def getFile[T](filename:String)(implicit m: Manifest[T]): T ={
    val source: BufferedSource = Source.fromFile(filename)
    val lines: String = try source.mkString
    finally source.close()
    JsonUtil.fromJson[T](lines)
  }

  def getDataInputStream(name: String): DataInputStream = {
    new DataInputStream(new FileInputStream(name))
  }

  def getDataOutputStream(name: String): DataOutputStream ={
    new DataOutputStream(new FileOutputStream(name))
  }

  def getLocalMatchListBitMap(name: String): Roaring64NavigableMap = {
    val localMatchListBitMap = new Roaring64NavigableMap
    localMatchListBitMap.deserialize(getDataInputStream(name))
    localMatchListBitMap
  }

  def setLocalMatchListBitMap(name: String, bitmap: Roaring64NavigableMap): Unit = {
    val out = new DataOutputStream(new FileOutputStream(name))
    bitmap.serialize(out)
    out.close()
  }
}
