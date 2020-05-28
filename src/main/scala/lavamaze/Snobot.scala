package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object Snobot {

  sealed trait Message
  case class MoveMessage(dir:Direction, dist:Int) extends Message

  val maskFill = "#7D5177"
  val maskStroke = "rgb(70,70,100)"
  val maskOpeningFill = "rgb(220, 200, 200)"
  val maskOpeningStroke ="rgb(70, 70, 100)"

  val ninjaHighlight = "rgba(0, 0, 255, 0.5)"

  val image = <("img")(^.src := "images/snobot.png").create()

  val dimension = 56

  private val _dx:Int = (oneTile - dimension) / 2
  private val _dy:Int = _dx

  def drawRight1(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 0, 0, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawRight2(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 64, 0, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawLeft1(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 128, 0, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawLeft2(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 192, 0, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawUp1(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 256, 0, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawUp2(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 0, 64, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawIdleL(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 64, 64, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawIdleR(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 128, 64, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawSmoke1(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 196, 64, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

  def drawSmoke2(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(Snobot.image, 256, 64, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

}


/**
 * Our programmable hero
 */
case class Snobot(maze:Maze) extends Mob with Askable[Snobot.Message, Boolean]{

  var px = 0
  var py = 0

  def tx = px / oneTile
  def ty = py / oneTile

  /** Updates snobot's location */
  def putAtTile(t:(Int, Int)):Unit = {
    val (xx, yy) = t
    px = xx * oneTile
    py = yy * oneTile
  }

  def cancel():Unit = if (!action.done) action.fail(new InterruptedException("Aborted"))

  def boundingBox:((Int, Int), (Int, Int)) = (px, py) -> (px + oneTile, py + oneTile)

  /** An Action that Snobot can perform */
  sealed trait Action extends Mob.Action

  /** The do nothing action */
  case class Idle() extends Action {
    var t = 0
    promise.success()

    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == 4) {
        val state = (t / tickRate) % 4
        if (state == 1) {
          Snobot.drawIdleL(px + x1, py + y1, ctx)
        } else if (state == 3) {
          Snobot.drawIdleR(px + x1, py + y1, ctx)
        } else {
          Snobot.drawUp2(px + x1, py + y1, ctx)
        }
      }
    }

    def tick(m:Maze): Unit = { t = t + 1 }
  }

  /** The do nothing action */
  case class AtGoal() extends Action {
    promise.success()

    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      // Snobot has left the building
    }

    def tick(m:Maze): Unit = { }
  }

  case class Move(d:Direction, dist:Int) extends Action {
    private val moveDuration = tickRate
    private val moveDistance = oneTile / moveDuration  // TODO: deal with floating point

    var t = 0

    override def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == 4) {
        val state = (t / 2) % 2
        d match {
          case EAST =>
            if (state == 1) Snobot.drawRight1(px + x1, py + y1, ctx) else Snobot.drawRight2(px + x1, py + y1, ctx)
          case WEST =>
            if (state == 1) Snobot.drawLeft1(px + x1, py + y1, ctx) else Snobot.drawLeft2(px + x1, py + y1, ctx)
          case _ =>
            if (state == 1) Snobot.drawUp1(px + x1, py + y1, ctx) else Snobot.drawUp2(px + x1, py + y1, ctx)
        }
      }
    }

    override def tick(m:Maze): Unit = {
      t = t + 1
      d match {
        case EAST => px += moveDistance
        case WEST => px -= moveDistance
        case NORTH => py -= moveDistance
        case SOUTH => py += moveDistance
      }

      if (t >= (dist * moveDuration)) promise.success()

    }
  }

  /** The do nothing action */
  case class Die() extends Action {
    var t = 0
    promise.success()

    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == 4) {
        if (t <= 4) {
          Snobot.drawSmoke1(px + x1, py + y1, ctx)
        } else if (t <= 8) {
          Snobot.drawSmoke2(px + x1, py + y1, ctx)
        }
      }
    }

    def tick(m:Maze): Unit = { t = t + 1 }
  }

  var action:Action = Move(EAST, 1)

  def alive:Boolean = action != Die()

  /**
   * Kills the Ninja if it is not on a passable square
   */
  def checkLocationValid():Unit = {
    if (alive && !maze.getTile(tx, ty).isPassableTo(this)) {
      action = Die()
    }
  }

  /**
   * Resets the Ninja to the start
   */
  def reset():Unit = {
    px = 0
    py = 0
    action = Idle()
  }

  def tick(maze:Maze) = {
    action.tick(maze)
    if (action.done) {
      action = action match {
        case Idle() => action
        case Die() => action
        case AtGoal() => action
        case _ => Idle()
      }
      checkLocationValid()
    }
  }

  def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = if (layer == 4) {
    action.paintLayer(layer, x1, y1, x2, y2, ctx)
  }

  /** Whether Snobot can move in a given direction */
  def canMove(d:Int):Boolean = alive && action.done && (d match {
    case EAST => maze.getTile(tx + 1, ty).isPassableTo(this)
    case WEST => maze.getTile(tx - 1, ty).isPassableTo(this)
    case SOUTH => maze.getTile(tx, ty + 1).isPassableTo(this)
    case NORTH => maze.getTile(tx, ty - 1).isPassableTo(this)
    case _ => false
  })

  def setAction(a:Action): Future[Unit] = {
    if (alive && action.done) {
      action = a
      a.future
    } else {
      Future.failed(new IllegalStateException("I'm sorry, I can't do that, Hal"))
    }
  }

  /** Send a message, with a callback for the reply */
  override def ask(message: Snobot.Message, receive: Boolean => Any): Unit = message match {
    case Snobot.MoveMessage(dir, dist) =>
      println("asked " + message)
      setAction(Move(dir, dist)).onComplete { _ => receive(alive) }
  }


  /** Send a message, with a callback for the reply */
  def askF(message: Snobot.Message): Future[Unit] = message match {
    case Snobot.MoveMessage(dir, dist) =>
      println("asked " + message)
      setAction(Move(dir, dist))
  }
}
