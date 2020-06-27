package canvasland
import canvasland.willtap.imperativeTopic.Vec2
import coderunner.Codable
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.concurrent.Future
import scala.scalajs.js

case class Turtle(initialX:Int, initialY:Int) extends Robot {

  var position:Vec2 = Vec2(initialX, initialY)

  override def reset():Unit = {
    action = Idle
    penRadius = 3
    pen = Some("rosybrown")
    position = Vec2(initialX, initialY)
    facing = 0
  }

  override def x:Double = position.x
  override def y:Double = position.y

  var facing:Double = 0
  var pen:Option[String] = Some("rosybrown")
  var penRadius:Double = 3
  var penDown:Boolean = true

  sealed trait Action extends Robot.Action

  case object Idle extends Action {
    override def tick(canvasLand: CanvasLand): Unit = {
      if (!promise.isCompleted) promise.success()
    }
  }

  case class ForwardAction(distance:Double, speed:Int = 1) extends Action {
    private var travelled:Double = 0
    private var increment = if (distance > 0) 1 else -1

    override def tick(canvasLand: CanvasLand): Unit = {
      for { _ <- 1 to speed } {
        ink(canvasLand)
        position += Vec2.fromRTheta(increment, facing)
        travelled += increment
        if (Math.abs(travelled) >= Math.abs(distance) && !promise.isCompleted) promise.success(())
      }
    }
  }

  case class TurnAction(angle:Double, speed:Int = 4) extends Action {
    private val destinationAngle = facing + angle
    private val tickInc = if (angle > 0) (Math.PI / 72) * speed else (-Math.PI / 25) * speed
    private var turned:Double = 0

    override def tick(canvasLand: CanvasLand): Unit = {
      turned = turned + Math.abs(tickInc)

      if (turned < Math.abs(angle)) {
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

    ctx.restore()

  }

  override def step(c:CanvasLand) = {
    action.tick(c)
    if (action.done) action = Idle
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
      c.fillCircle(position, penRadius, p)
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
      ("forward", Seq("number"), (x:Int) => ask(Turtle.Forward(x)).toJSPromise),
      ("back", Seq("number"), (x:Int) => ask(Turtle.Forward(-x)).toJSPromise),
      ("clockwise", Seq("number"), (x:Double) => ask(Turtle.Clockwise(x)).toJSPromise),
      ("anticlockwise", Seq("number"), (x:Double) => ask(Turtle.Anticlockwise(x)).toJSPromise),
      ("right", Seq("number"), (x:Double) => ask(Turtle.Clockwise(Vec2.toRadians(x))).toJSPromise),
      ("left", Seq("number"), (x:Double) => ask(Turtle.Anticlockwise(Vec2.toRadians(x))).toJSPromise),
      ("setColour", Seq("string"), (x:String) => { pen = Some(x); Future.successful().toJSPromise }),
      ("setThickness", Seq("number"), (x:Double) => { penRadius = x; Future.successful().toJSPromise }),
      ("penUp", Seq.empty, () => { penDown = false; Future.successful().toJSPromise }),
      ("penDown", Seq.empty, () => { penDown = true; Future.successful().toJSPromise }),
    )
  }
}

object Turtle {

  sealed trait Message
  case class Forward(x:Int) extends Message
  case class Backward(x:Int) extends Message
  case class Clockwise(x:Double) extends Message
  case class Anticlockwise(x:Double) extends Message
  case class Colour(s:String) extends Message
  case class Thickness(r:Double) extends Message
  case object NoPen extends Message

  def turtleException(msg:String) = new js.JavaScriptException(msg)

}
