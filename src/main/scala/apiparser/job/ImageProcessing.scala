package apiparser.job

import apiparser.util.FileUtil
import java.awt.{AlphaComposite, Color, Image, Shape}
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.collection.mutable

/**
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=ImageProcessing --execution-date=2019-09-25"
  *
  *  Run with Jar:
  *  java -cp dist/apiparser.jar apiparser.Main "--job-name=ImageProcessing" "--execution-date=2019-09-25"
  */
class ImageProcessing extends GeneralJob {
  override def whoami(): Unit = {
    println("This is print from image processing job")
  }

  val iconSize = 32

  val angleMap = Map(
    0 -> (10, 17),
    1 -> (7, 17),
    2 -> (3, 10),
    3 -> (7, 3),
    4 -> (10, 3),
    5 -> (17, 7),
    6 -> (17, 10),
    7 -> (17, 13),
  )

  val minimapLocation = Map(
    1 -> (238,238),
    2 -> (145,163),
    3 -> (356,315),
    4 -> (151,245),
    5 -> (82,401),
    6 -> (239,116),
    7 -> (397,231),
    8 -> (400,113),
    9 -> (302,373),
  )

  def getImage(fileName: String):BufferedImage = {
    ImageIO.read(getClass.getClassLoader.getResourceAsStream(fileName))
  }

  def copyImage(input: BufferedImage): BufferedImage = {
    val w = input.getWidth()
    val h = input.getHeight()
    val output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    for (x <- 0 until w){
      for (y <- 0 until h) {
        output.setRGB(x, y, input.getRGB(x,y))
      }
    }
    output
  }

  def getCircle(input: BufferedImage, x:Int, y:Int): BufferedImage ={
    val w = input.getWidth()
    val h = input.getHeight()
    val output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = output.createGraphics
    g2.setComposite(AlphaComposite.Src)
    g2.setClip(new Ellipse2D.Float(x, y, w, h))
    g2.drawImage(input, 0, 0, w, h, null)
    g2.dispose()
    output
  }

  def drawCircle(x:Int, y:Int, color: Color): BufferedImage ={
    val w = x
    val h = y
    val output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = output.createGraphics
    g2.setColor(color)
    g2.fillOval(0,0,w,h)
//    g2.setClip(new Ellipse2D.Float(0, 0, w, h))
//    g2.drawImage(output, 0, 0, w, h, null)

    g2.dispose()

//    val inner = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
//    val g3 = inner.createGraphics()
//    g3.setComposite(AlphaComposite.Src)
//    g3.setBackground(color)
//    g3.setClip(new Ellipse2D.Float(1, 1, w-2, h-2))
//    g3.dispose()
//
//    for (x <- 0 until w){
//      for (y <- 0 until h) {
//        val color = if (output.getRGB(x, y) != inner.getRGB(x,y)) output.getRGB(x,y) else output.getRGB(0,0)
//        output.setRGB(x, y, color)
//      }
//    }
    output
  }

  /**
    *
    * return the remaining part from cutting a part of circle
    *
    *
    *
    *      ***       ***
    *     *****  -> ***
    *      ***       ***
    *
    * @param input
    * @param angle: 0 - 7, reach 1 represent 45 degree increase clockwise cut a circle
    * @return
    */
  def cutCirclePart(input: BufferedImage, angle: Int): BufferedImage ={
    val w = input.getWidth()
    val h = input.getHeight()
    val output = copyImage(input)
    val intersection = getCircle(
      output,
      w * (10 - angleMap(angle)._1) / 10,
      h * (10 - angleMap(angle)._2) / 10)

    for (x <- 0 until w){
      for (y <- 0 until h) {
        val color = if (output.getRGB(x, y) != intersection.getRGB(x,y)) output.getRGB(x,y) else output.getRGB(0,0)
        output.setRGB(x, y, color)
      }
    }
    output
  }

  def scaleToIcon(input: BufferedImage): BufferedImage = {
    val resized = input.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT)
    val w = resized.getWidth(null)
    val h = resized.getHeight(null)
    val output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = output.createGraphics
    g2.drawImage(resized, 0, 0, w, h, null)
    g2.dispose()
    output
  }

  def getIconOnMap(input: BufferedImage): BufferedImage ={
    val w = input.getWidth
    val h = input.getHeight
    val outter_circle = drawCircle(w+6, h+6, new Color(0x64969C))
    val mid_circle = drawCircle(w+4,h+4, new Color(0x4D7E8C))
    val inner_cicle = drawCircle(w+2,h+2,new Color(0x1D3F58))

    val grah0 = inner_cicle.getGraphics
    grah0.drawImage(input, 1, 1, w, h, null)
    grah0.dispose()

    val grah1 = mid_circle.getGraphics
    grah1.drawImage(inner_cicle, 1, 1, w+2, h+2, null)
    grah1.dispose()

    val grah2 = outter_circle.getGraphics
    grah2.drawImage(mid_circle, 1, 1, w+4, h+4, null)
    grah2.dispose()

    outter_circle
  }

  def getMiniMapLocation(index: Int): (Double,Double,Double,Double) = {
    val leftUpperX = minimapLocation(index)._1 / 512.0
    val leftUpperY = minimapLocation(index)._2 / 512.0
    val rightBottomX = leftUpperX + iconSize / 512.0
    val rightBottomY = leftUpperY + iconSize / 512.0
    (leftUpperX,leftUpperY,rightBottomX,rightBottomY)
  }

  override def execute(jobArgs: JobArgs): Unit = {
    val champion = "Akali"
    val labels = new mutable.ListBuffer[String]

    val input = getImage(s"$champion.png")
    ImageIO.write(scaleToIcon(input), "png", new File(s"processed/${champion}_00.png"))
    labels.append(s"TRAIN,gs://champion_meta/processed/${champion}_00.png,${champion.toLowerCase()},0,0,,,1,1,,")

    val raw_circle = scaleToIcon(getCircle(input, 0, 0))

    val circle = getIconOnMap(raw_circle)
    val icon_w = circle.getWidth()
    val icon_h = circle.getHeight()
    ImageIO.write(circle, "png", new File(s"processed/${champion}_01.png"))
    labels.append(s"TRAIN,gs://champion_meta/processed/${champion}_01.png,akali,0,0,,,1,1,,")

    val map = getImage("map11.png")

    val iconMap = new mutable.HashMap[Int, BufferedImage]()

    for(i <- 0 until 8) {
      val cutCircleRight = cutCirclePart(circle, i)
      iconMap.put(i, cutCircleRight)
      ImageIO.write(cutCircleRight, "png", new File(s"processed/${ champion }_0${ i + 2 }.png"))
      labels.append(s"TRAIN,gs://champion_meta/processed/${ champion }_0${ i + 2 }.png,akali,0,0,,,1,1,,")
    }

    for ((k, v) <- minimapLocation) {
      val tempMap = copyImage(map)

      val g = tempMap.getGraphics
      g.drawImage(circle, v._1, v._2, icon_w, icon_h, null)
      g.dispose()
      ImageIO.write(tempMap, "png", new File(s"processed/${champion}_${k}1.png"))

      for(i <- 0 until 8){
        val cutCircleRight = iconMap(i)
        val tempMap2 = copyImage(map)
        val g2 = tempMap2.getGraphics
        g2.drawImage(cutCircleRight, v._1, v._2, icon_w, icon_h, null)
        g2.dispose()
        ImageIO.write(tempMap2, "png", new File(s"processed/${champion}_$k${i+2}.png"))

        val location = getMiniMapLocation(k)

        i match {
          case 6 => labels.append(getLabelLine("VALIDATE", champion, k, i+2, location))
          case 7 => labels.append(getLabelLine("TEST", champion, k, i+2, location))
          case _ => labels.append(getLabelLine("TRAIN", champion, k, i+2, location))
        }
      }
    }

    FileUtil.writeFile(s"labels.csv",
      labels,
      append = false)
  }

  def getLabelLine(dataset: String, champion: String, index1: Int, index2: Int, location:(Double,Double,Double,Double)): String = {
    f"${dataset},gs://champion_meta/processed/${ champion }_${index1}${index2}.png,${champion.toLowerCase()},${location._1}%1.3f,${location._2}%1.3f,,,${location._3}%1.3f,${location._4}%1.3f,,"
  }
}
