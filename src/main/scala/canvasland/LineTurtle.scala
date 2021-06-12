package canvasland

import coderunner.Codable
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.util.{Random, Try}

/**
 * LineBot has two wheels, driven by DC motors, and configurable line and obstacle sensors in front of it
 */
case class LineTurtle(initialPos:(Double, Double))(config: LineTurtle => Unit) extends Robot {

  private var position:Vec2 = Vec2(initialPos._1, initialPos._2)
  private var facing:Double = 0

  override def x:Double = position.x
  override def y:Double = position.y
  def angle:Double = facing


  /** On each tick as the robot moves, it's angle will be impacted by +/- up to this many radians of variation */
  var moveWobble:Double = 0

  /** On each tick as the robot moves, it will tend to veer its angle by this amount */
  var moveWobbleBias:Double = 0

  /** Requests to move will be inaccurate by +/- up to this fraction */
  var moveInaccuracy:Double = 0

  /** Requests to move will be biased by this fraction */
  var moveBias:Double = 0

  /** Requests to move will be inaccurate by +/- up to this fraction */
  var turnInaccuracy:Double = 0

  /** Requests to turn will be biased by this fraction */
  var turnBias:Double = 0

  /** If non-negative, limits the number of sensors that can be added to the turtle  */
  var sensorLimit:Int = 8

  /** Calculates an angle to wobble by on each step */
  private def tickMoveWobble:Double = {
    if (moveWobble > 0) {
      (Random.nextDouble - 0.5) * moveWobble * 2 + moveWobbleBias
    } else moveWobbleBias
  }

  /** Muddies a move command */
  private def innacurateMove(d:Double):Double = {
    if (moveInaccuracy != 0) {
      (1 + moveBias + (Random.nextDouble() - 0.5) * moveInaccuracy * 2) * d
    } else d * (1 + moveBias)
  }

  /** Muddies a turn command */
  private def innacurateTurn(d:Double):Double = {
    if (turnInaccuracy != 0) {
      (1 + turnBias + (Random.nextDouble() - 0.5) * turnInaccuracy * 2) * d
    } else d * (1 + turnBias)
  }

  def setAngle(a:Double):Unit = {
    facing = a
  }

  def setPosition(x:Double, y:Double):Unit = {
    position = Vec2(x, y)
  }

  private var compositeMode:Option[String] = None

  trait Sensor {
    def value:Double

    def paint(ctx:CanvasRenderingContext2D):Unit

    def tick(p:Vec2, c:CanvasLand):Unit
  }

  /**
   * A line sensor looks at the canvas underneath the robot and attempts to read pixel data from it
   * @param dp - the relative position of the sensor, compared to the centre of the robot
   * @param radius - the sensor reads a square of pixel data, (2 * radius + 1) by (2 * radius + 1) in size.
   *                 This rectangle is always aligned with the underlying canvas (it does not rotate)
   */
  class LineSensor(dp:Vec2, radius:Int = 3, val r:Double = 1, g:Double = 1, b:Double = 0) extends Sensor {

    val totalSensitivity:Double = r + g + b
    val strokeStyle = s"rgb($r,$g,$b)"

    private var lastReading:Double = Double.NaN
    def value = lastReading

    // Measures the pixel, weighted by the sensitivities
    private def measurePixel(rr:Int, gg:Int, bb:Int, a:Int):Double = {
      (a / 255).toDouble * ((r * rr / 255) + (g * gg / 255) + (b * bb / 255)).toDouble / totalSensitivity
    }

    def canvasPoint:Vec2 = position + dp.rotate(angle)

    /** On each tick, the sensor reads from the canvas */
    def tick(p:Vec2, c:CanvasLand) = {
      val point = canvasPoint
      val x = (point.x + 0.5).toInt
      val y = (point.y + 0.5).toInt

      c.withCanvasContext {
        ctx =>
          val imageData = ctx.getImageData(x - radius, y - radius, 1 + 2 * radius, 1 + 2 * radius).data
          //println(imageData.length)
          val measurements = for {
            i <- imageData.indices by 4
          } yield {
            val r = imageData(i)
            val g = imageData(i + 1)
            val b = imageData(i + 2)
            val a = imageData(i + 3)
            measurePixel(r, g, b, a)
          }
          lastReading = measurements.sum / measurements.length
      }
    }

    def paint(ctx:CanvasRenderingContext2D): Unit = {
      val drawRadius = 5

      ctx.save()
      ctx.strokeStyle = strokeStyle
      ctx.beginPath()
      ctx.arc(dp.x, dp.y, drawRadius, 0, 2 * Math.PI)
      ctx.stroke()
      if (lastReading >= 0 && lastReading <= 1) {
        val shade = (255 * lastReading).toInt
        ctx.fillStyle = s"rgb($shade, $shade, $shade)"
        ctx.fill()
      }
      //val tp = dp + Vec2(20, 0)
      //ctx.fillText(lastReading.toString, tp.x, tp.y, 100)
      ctx.restore()
    }

  }

  val sensors:mutable.Buffer[Sensor] = mutable.Buffer.empty

  override def reset():Unit = {
    action = Idle
    penRadius = 3
    pen = Some("rosybrown")
    penDown = true
    position = Vec2(initialPos._1, initialPos._2)
    facing = 0
    sensors.clear()
    config(this)
  }

  var pen:Option[String] = Some("rosybrown")
  var penRadius:Double = 3
  var penDown:Boolean = true

  sealed trait Action extends Robot.Action

  case object Idle extends Action {
    override def tick(canvasLand: CanvasLand): Unit = {
      if (!promise.isCompleted) promise.success(())
    }
  }

  case class ForwardAction(distance:Double, speed:Int = 1) extends Action {
    private var travelled:Double = 0
    private val total = Math.abs(innacurateMove(distance))
    private var sign = if (distance > 0) 1 else -1

    override def tick(canvasLand: CanvasLand): Unit = {
      for { _ <- 1 to speed } {
        ink(canvasLand)
        val increment:Double = if (total - travelled > 1) sign else (total - travelled) * sign
        position += Vec2.fromRTheta(increment, facing)
        travelled += increment
        facing += tickMoveWobble
        if (Math.abs(travelled) >= total && !promise.isCompleted) promise.success(())
      }
    }
  }

  case class TurnAction(angle:Double, speed:Int = 4) extends Action {
    val muddied = innacurateTurn(angle)
    private val destinationAngle = facing + muddied
    private val tickInc = if (muddied > 0) (Math.PI / 72) * speed else (-Math.PI / 25) * speed
    private var turned:Double = 0

    override def tick(canvasLand: CanvasLand): Unit = {
      turned = turned + Math.abs(tickInc)

      if (turned < Math.abs(muddied)) {
        facing += tickInc
      } else {
        facing = destinationAngle
        if (!promise.isCompleted) promise.success(())
      }
    }
  }

  var action:Action = Idle

  override def draw(ctx: CanvasRenderingContext2D): Unit = {
    ctx.save()
    ctx.rotate(facing)
    ctx.strokeStyle = "black"
    ctx.lineWidth = 2
    ctx.fillStyle = "rgba(240, 248, 255, 0.6)"

    ctx.beginPath()
    ctx.moveTo(25,0)
    ctx.bezierCurveTo(-5, 30, -15, 5, -15, 0)
    ctx.bezierCurveTo(-15, -5, -5, -30, 25, 0)
    ctx.fill()
    ctx.stroke()

    pen match {
      case Some(c) =>
        ctx.fillStyle = c
        ctx.strokeStyle = if (penDown) "black" else c
        ctx.lineWidth = 1
        ctx.beginPath()
        ctx.arc(0, 0, penRadius, 0, Math.PI * 2)
        if (penDown) ctx.fill()
        ctx.stroke()
      case _ =>
        ctx.fillStyle = "black"
        ctx.arc(0, 0, 1, 0, Math.PI * 2)
        ctx.fill()
    }

    for { ls <- sensors } ls.paint(ctx)
    ctx.restore()

  }

  override def step(c:CanvasLand) = {
    action.tick(c)
    if (action.done) action = Idle

    for { ls <- sensors.iterator } ls.tick(position, c)
  }

  def setAction(a:Action): Future[Unit] = {
    if (action.done) {
      action = a
      a.future
    } else {
      Future.failed(new IllegalStateException(s"I'm busy doing $action"))
    }
  }

  def ink(c:CanvasLand):Unit = {
    if (penDown) for {
      p <- pen
    } {
      c.fillCircle(position, penRadius, p, compositeMode)
    }
  }


  def ask(message: Turtle.Message): Future[Unit] = message match {
    case Turtle.Forward(x) => {
      setAction(this.ForwardAction(x))
    }
    case Turtle.Clockwise(x) =>
      setAction(this.TurnAction(x))
    case Turtle.Anticlockwise(x) =>
      setAction(this.TurnAction(-x))
    case _ =>
      Future.failed(Turtle.turtleException(s"Unknown command $message"))
  }

  override def functions(): Seq[Codable.Triple] = {
    import scala.scalajs.js.JSConverters._
    import scala.concurrent.ExecutionContext.Implicits.global

    Seq(
      ("forward", Seq("number"), (x:Double) => ask(Turtle.Forward(x)).toJSPromise),
      ("back", Seq("number"), (x:Double) => ask(Turtle.Forward(-x)).toJSPromise),
      ("clockwise", Seq("number"), (x:Double) => ask(Turtle.Clockwise(x)).toJSPromise),
      ("anticlockwise", Seq("number"), (x:Double) => ask(Turtle.Anticlockwise(x)).toJSPromise),
      ("right", Seq("number"), (x:Double) => ask(Turtle.Clockwise(Vec2.toRadians(x))).toJSPromise),
      ("left", Seq("number"), (x:Double) => ask(Turtle.Anticlockwise(Vec2.toRadians(x))).toJSPromise),
      ("setColour", Seq("string"), (x:String) => { pen = Some(x); Future.successful(()).toJSPromise }),
      ("setThickness", Seq("number"), (x:Double) => { penRadius = x; Future.successful(()).toJSPromise }),
      ("penUp", Seq.empty, () => { penDown = false; Future.successful(()).toJSPromise }),
      ("penDown", Seq.empty, () => { penDown = true; Future.successful(()).toJSPromise }),

      ("addLineSensor", Seq("number", "number", "number", "number", "number"), (x:Double, y:Double, r:Int, g:Int, b:Int) => {
        Future.fromTry(Try {
          if (sensorLimit < 0 || sensors.length < sensorLimit) {
            sensors.append(new LineSensor(Vec2(x, y), r = r, g = g, b = b));
          } else {
            throw new JavaScriptException(s"Sorry, you are limited to $sensorLimit sensors")
          }
          ()
        }).toJSPromise
      }),

      ("readSensor", Seq("number"), (i:Int) => {
        Future.fromTry(Try {
          sensors(i).value
        }).toJSPromise
      }),

      ("setCompositeMode", Seq("string"), (x:String) => {
        Future.fromTry(Try {
          compositeMode = Option.apply(x)
        }).toJSPromise
      })
    )
  }
}

object LineTurtle {

  sealed trait Message
  case class Forward(x:Double) extends Message
  case class Backward(x:Double) extends Message
  case class Clockwise(x:Double) extends Message
  case class Anticlockwise(x:Double) extends Message
  case class Colour(s:String) extends Message
  case class Thickness(r:Double) extends Message
  case object NoPen extends Message

  def turtleException(msg:String) = new js.JavaScriptException(msg)

}
