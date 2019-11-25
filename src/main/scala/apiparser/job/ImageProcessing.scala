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

  // This is a hardcoded size for lol champion portrait only
  val iconSize = 32

  // Tricky part, these number used to figure erosion, these 8 points represent 8 circles' center
  // with 45 degree clock-wise, detail please take a look at @cutCirclePart
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

  // These are just randomly select coordinate, you can change to any number < 512
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

  /**
    * Get the image with the file name (under resource folder)
    * @param fileName
    * @return
    */
  def getImage(fileName: String):BufferedImage = {
    ImageIO.read(getClass.getClassLoader.getResourceAsStream(fileName))
  }

  /**
    * Return a copy of image
    * @param input
    * @return
    */
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

  /**
    * Cut the original picture into a circle shape, new circle use the original picture's width
    * and height as the ellipse's width and height framing rectangle
    * @param input
    * @param x: x-coordinate of the upper-left corner where the circle start to draw
    * @param y: y-coordinate of the upper-left corner where the circle start to draw
    * @return
    */
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

  /**
    * This method will return a oval picture, if x=y, then it is a circle
    * @param x: the width of the framing rectangle
    * @param y: the height of the framing rectangle
    * @param color: color of ring
    * @return
    */
  def drawCircle(x:Int, y:Int, color: Color): BufferedImage ={
    val w = x
    val h = y
    val output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = output.createGraphics
    g2.setColor(color)
    g2.fillOval(0,0,w,h)

    g2.dispose()
    output
  }

  /**
    *
    * return the remaining part from cutting a part of circle
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

  /**
    * Scale the picture to the @iconSize
    * @param input
    * @return
    */
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

  /**
    * Still, maybe bad name... This method will return one similar champion portrait which the real
    * game mini map will use, but not the same, I just try to add 3 color rings on the outside the
    * champion portrait, there will be some color different from the one in the video or game
    * @param input
    * @return
    */
  def getIconOnMap(input: BufferedImage): BufferedImage ={
    val w = input.getWidth
    val h = input.getHeight
    val outter_circle = drawCircle(w+6, h+6, new Color(0x64969C))
    val mid_circle = drawCircle(w+4,h+4, new Color(0x4D7E8C))
    val inner_cicle = drawCircle(w+2,h+2,new Color(0x1D3F58))

    // forgive me, I'm lazy. these three grah 0, 1, 2 just 3 circle with different circle and put
    // at the same center then out put as outter_circle
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

  /**
    * get the relative position of the target composed to the map, this one use the hard coded
    * @minimapLocation, the later training process need use this coordinate
    *
    * @param index
    * @return
    */
  def getMiniMapLocation(index: Int): (Double,Double,Double,Double) = {
    val leftUpperX = minimapLocation(index)._1 / 512.0
    val leftUpperY = minimapLocation(index)._2 / 512.0
    val rightBottomX = leftUpperX + iconSize / 512.0
    val rightBottomY = leftUpperY + iconSize / 512.0
    (leftUpperX,leftUpperY,rightBottomX,rightBottomY)
  }

  override def execute(jobArgs: JobArgs): Unit = {
    // current hardcoded one label
    val champion = "Akali"
    val labels = new mutable.ListBuffer[String]

    // Read the resource champion portrait, cut it to a circle shape
    val input = getImage(s"$champion.png")
    ImageIO.write(scaleToIcon(input), "png", new File(s"processed/${champion}_00.png"))
    labels.append(s"TRAIN,gs://champion_meta/processed/${champion}_00.png,${champion.toLowerCase()},0,0,,,1,1,,")
    val raw_circle = scaleToIcon(getCircle(input, 0, 0))

    // Transform the raw circle to the mini map icon display shape
    val circle = getIconOnMap(raw_circle)
    val icon_w = circle.getWidth()
    val icon_h = circle.getHeight()

    // Output as one picture as training purpose
    ImageIO.write(circle, "png", new File(s"processed/${champion}_01.png"))
    labels.append(s"TRAIN,gs://champion_meta/processed/${champion}_01.png,akali,0,0,,,1,1,,")

    // Read map picture
    val map = getImage("map11.png")

    // These part will produce some champion portrait with circle shape erosion on certain angle
    // You can find what is is looks like in the output file
    val iconMap = new mutable.HashMap[Int, BufferedImage]()
    for(i <- 0 until 8) {
      val cutCircleRight = cutCirclePart(circle, i)
      iconMap.put(i, cutCircleRight)
      ImageIO.write(cutCircleRight, "png", new File(s"processed/${ champion }_0${ i + 2 }.png"))
      labels.append(s"TRAIN,gs://champion_meta/processed/${ champion }_0${ i + 2 }.png,akali,0,0,,,1,1,,")
    }

    /**
      * By now we already read the Map.png, champion portrait and 8 eroded champion portraits in
      * memory, we will proceed the compose part below
      */

    // Put the icons on to mini map
    for ((k, v) <- minimapLocation) {
      //copy the map image
      val tempMap = copyImage(map)
      val g = tempMap.getGraphics

      //put the circle champion portrait onto map
      g.drawImage(circle, v._1, v._2, icon_w, icon_h, null)
      g.dispose()
      ImageIO.write(tempMap, "png", new File(s"processed/${champion}_${k}1.png"))

      // put 8 eroded portrait onto map
      for(i <- 0 until 8){
        val cutCircleRight = iconMap(i)
        val tempMap2 = copyImage(map)
        val g2 = tempMap2.getGraphics
        g2.drawImage(cutCircleRight, v._1, v._2, icon_w, icon_h, null)
        g2.dispose()
        ImageIO.write(tempMap2, "png", new File(s"processed/${champion}_$k${i+2}.png"))

        // Get the relative location
        val location = getMiniMapLocation(k)
        // This is the hard coded part original from GCP obj detection api, in order to train
        // model, they need train/test/validation datasets, split the our composed pictures into
        // these three group by some hardcoded group 0-5 as train, 6 as validation, 7 as test
        i match {
          case 6 => labels.append(getLabelLine("VALIDATE", champion, k, i+2, location))
          case 7 => labels.append(getLabelLine("TEST", champion, k, i+2, location))
          case _ => labels.append(getLabelLine("TRAIN", champion, k, i+2, location))
        }
      }
    }

    // Out put the csv file
    FileUtil.writeFile(s"labels.csv",
      labels,
      append = false)
  }

  /**
    * Get the formatted line of the output csv file
    * @param dataset: Which dataset this file will be used for VALIDATE/TEST/TRAIN
    * @param champion: Label we marked on in the file
    * @param index1: err... bad practise, the original picture I only plan to use 00-99 as the
    *              suffix, in order to do an easy format, I split them into 2 digit, this is the
    *              most significant one
    * @param index2: please read index1, this is the least significant one
    * @param location: location obj(top_left_x, top_left_y, bottom_right_x, bottom_right_y)
    * @return
    */
  def getLabelLine(dataset: String, champion: String, index1: Int, index2: Int, location:(Double,Double,Double,Double)): String = {
    f"${dataset},gs://champion_meta/processed/${ champion }_${index1}${index2}.png,${champion.toLowerCase()},${location._1}%1.3f,${location._2}%1.3f,,,${location._3}%1.3f,${location._4}%1.3f,,"
  }
}
