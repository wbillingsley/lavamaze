package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import lavamaze.BlobGuard.{dimension, image}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object Dogbot {

  sealed trait Message
  case class MoveMessage(dir:Direction) extends Message

  private val image = loadImage("dogbot.png")

  val dimension = 56

  private val _dx:Int = (oneTile - dimension) / 2
  private val _dy:Int = _dx

  def drawImage(ix:Int, iy:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, ix * 64, iy * 64, 64, 64, offsetX + _dx, offsetY + _dy, dimension, dimension)
  }

}


/**
 * Our programmable hero
 */
case class Dogbot(maze:Maze, initX:Int, initY:Int) extends GridMob with Askable[Dogbot.Message, Unit]{

  var px:Int = initX * oneTile
  var py:Int = initY * oneTile

  def tx = px / oneTile
  def ty = py / oneTile

  private val hitBoxSize = 16
  private val dhb = (oneTile - hitBoxSize) / 2

  def hitBox:((Int, Int), (Int, Int)) = (px + dhb, py + dhb) -> (px + oneTile - dhb, py + oneTile - dhb)

  /** Updates snobot's location */
  def putAtTile(t:(Int, Int)):Unit = {
    val (xx, yy) = t
    px = xx * oneTile
    py = yy * oneTile
  }

  def cancel():Unit = if (!action.done) action.fail(new InterruptedException("Aborted"))

  def boundingBox:((Int, Int), (Int, Int)) = (px, py) -> (px + oneTile, py + oneTile)

  /** An Action that Snobot can perform */
  sealed trait Action extends GridAction

  /** The do nothing action */
  case class Idle() extends GridIdle() with Action {
    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == MOB_LOW) {
        Dogbot.drawImage(0, 1)(px + x1, py + y1, ctx)
      }
    }
  }

  /** The do nothing action */
  case class AtGoal() extends GridIdle() with Action  {
    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      // Snobot has left the building
    }
  }

  case class Move(d:Direction) extends GridMove(d, tickRate) with Action {
    override def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == MOB_LOW) {
        val state = (t / 2) % 2
        d match {
          case EAST =>
            if (state == 1) {
              Dogbot.drawImage(1, 0)(px + x1, py + y1, ctx)
            } else {
              Dogbot.drawImage(0, 0)(px + x1, py + y1, ctx)
            }
          case WEST =>
            if (state == 1) {
              Dogbot.drawImage(1, 1)(px + x1, py + y1, ctx)
            } else {
              Dogbot.drawImage(2, 1)(px + x1, py + y1, ctx)
            }
          case _ =>
            if (state == 1) {
              Dogbot.drawImage(2, 0)(px + x1, py + y1, ctx)
            } else {
              Dogbot.drawImage(0, 1)(px + x1, py + y1, ctx)
            }
        }
      }
    }
  }

  /** The do nothing action */
  case class Die() extends GridIdle() with Action  {

    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == MOB_LOW) {
        if (t <= 4) {
          Snobot.drawSmoke1(px + x1, py + y1, ctx)
        } else if (t <= 8) {
          Snobot.drawSmoke2(px + x1, py + y1, ctx)
        }
      }
    }

    override def destination: (Direction, Direction) = (tx, ty)
  }

  var action:Action = Idle()

  def snobotException(msg:String) = new js.JavaScriptException(msg)

  def alive:Boolean = action != Die()

  def die():Unit = action match {
    case Die() => // do nothing
    case _ => action = Die()
  }

  /**
   * Kills the Ninja if it is not on a passable square
   */
  def interactWithTiles():Unit = {
    if (alive) {
      maze.cellsIntersecting(hitBox).foreach(_.actUpon(this))
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
    interactWithTiles()
    if (action.done) {
      action = action match {
        case Idle() => action
        case Die() => action
        case AtGoal() => action
        case _ => Idle()
      }
    }
  }

  def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
    action.paintLayer(layer, x1, y1, x2, y2, ctx)
  }

  /** Whether Snobot can move in a given direction */
  def canMove(d:Int):Boolean = !isBlocked(d) && (d match {
    case EAST => maze.getTile(tx + 1, ty).isPassableTo(this)
    case WEST => maze.getTile(tx - 1, ty).isPassableTo(this)
    case SOUTH => maze.getTile(tx, ty + 1).isPassableTo(this)
    case NORTH => maze.getTile(tx, ty - 1).isPassableTo(this)
    case _ => false
  })

  /** Whether Snobot can move in a given direction */
  def isBlocked(d:Int):Boolean = alive && action.done && (d match {
    case EAST => maze.blockMovement((tx, ty), (tx + 1, ty), this)
    case WEST => maze.blockMovement((tx, ty), (tx - 1, ty), this)
    case SOUTH => maze.blockMovement((tx, ty), (tx, ty + 1), this)
    case NORTH => maze.blockMovement((tx, ty), (tx, ty - 1), this)
    case _ => false
  })

  override def blockMovement(from: (Direction, Direction), to: (Direction, Direction), by: Mob): Boolean = {
    by match {
      case _ => (tx, ty) == to || action.destination == to
    }
  }

  def setAction(a:Action): Future[Unit] = {
    if (alive && action.done) {
      action = a
      a.future
    } else {
      Future.failed(new IllegalStateException("I'm sorry, I can't do that, Hal"))
    }
  }


  /** Send a message, with a callback for the reply */
  def ask(message: Dogbot.Message): Future[Unit] = message match {
    case Dogbot.MoveMessage(dir) =>
      val a = Move(dir)
      for { m <- maze.mobsInTile(a.destination) } m match {
        case b:Boulder => b.push(dir)
        case _ => // do nothing
      }
      if (!isBlocked(dir)) setAction(a) else Future.failed(snobotException("Dogbot is blocked in that direction"))
  }
}
