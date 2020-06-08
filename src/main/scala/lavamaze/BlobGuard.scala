package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.concurrent.Future
import scala.util.Random

object BlobGuard {
  val image = <("img")(^.src := "images/blobguard.png").create()

  val dimension = 56
  val offset = (oneTile - dimension) / 2

  def drawImage(ix:Int, iy:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, ix * 64, iy * 64, 64, 64, offsetX + offset, offsetY + offset, dimension, dimension)
  }

  def drawIdle(i:Int, x:Int, y:Int, ctx:dom.CanvasRenderingContext2D) = {
    if (i % 2 == 0) drawImage(1, 1)(x, y, ctx)
    else if (i % 4 == 1) drawImage(2, 1)(x, y, ctx)
    else drawImage(0, 2)(x, y, ctx)
  }

  def drawLeft(i:Int, x:Int, y:Int, ctx:dom.CanvasRenderingContext2D) = {
    if (i % 2 == 0) drawImage(0, 0)(x, y, ctx)
    else drawImage(1, 0)(x, y, ctx)
  }

  def drawRight(i:Int, x:Int, y:Int, ctx:dom.CanvasRenderingContext2D) = {
    if (i % 2 == 0) drawImage(2, 0)(x, y, ctx)
    else drawImage(0, 1)(x, y, ctx)
  }

  def drawDie(i:Int, x:Int, y:Int, ctx:dom.CanvasRenderingContext2D) = {
    i match {
      case 0 => drawImage(1, 2)(x, y, ctx)
      case 1 => drawImage(2, 2)(x, y, ctx)
      case 2 => drawImage(0, 3)(x, y, ctx)
      case _ => // do nothing
    }
  }

  val patrolAI = (m:Maze, b:BlobGuard) => {
    b.action match {
      case b.Move(d) if b.canMove(d) => b.setAction(b.Move(d))
      case b.Idle() =>
        val d = Seq(NORTH, SOUTH, EAST, WEST)(Random.nextInt(4))
        if (b.canMove(d)) b.setAction(b.Move(d))
      case _ => b.setAction(b.Idle())
    }
  }

  val zeroInAI = (m:Maze, b:BlobGuard) => {
    if (m.snobot.tx < b.tx && b.canMove(WEST)) b.setAction(b.Move(WEST))
    else if (m.snobot.tx > b.tx && b.canMove(EAST)) b.setAction(b.Move(EAST))
    else if (m.snobot.ty < b.ty && b.canMove(NORTH)) b.setAction(b.Move(NORTH))
    else if (m.snobot.ty > b.ty && b.canMove(SOUTH)) b.setAction(b.Move(SOUTH))
    else if (b.action != b.Idle()) b.setAction(b.Idle())
  }
}

class BlobGuard(maze:Maze, initTx:Int, initTy:Int)(ai: (Maze, BlobGuard) => _) extends GridMob {

  var px = initTx * oneTile
  var py = initTy * oneTile

  def hitBox = ((px + 4, py + 4), (px + oneTile - 4, py + oneTile - 4))

  def tx = px / oneTile
  def ty = py / oneTile

  sealed trait Action extends GridAction

  /** The do nothing action */
  case class Idle() extends GridIdle() with Action {
    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == MOB_LOW) {
        val state = (t / 8) % 4
        BlobGuard.drawIdle(state, px -x1, py - y1, ctx)
      }
    }
  }

  case class Move(d:Direction) extends GridMove(d, tickRate) with Action {
    override def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == MOB_LOW) {
        val state = (t / 8)
        d match {
          case WEST => BlobGuard.drawLeft(state, px + x1, py + y1, ctx)
          case EAST => BlobGuard.drawRight(state, px + x1, py + y1, ctx)
          case _ => BlobGuard.drawIdle(state, px + x1, py + y1, ctx)
        }
      }
    }
  }

  /** The do nothing action */
  case class Die() extends GridIdle with Action {
    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == MOB_LOW) BlobGuard.drawDie(t / 4, px - x1, py - y1, ctx)
    }
  }

  /** Whether BlobGuard can move in a given direction */
  def canMove(d:Int):Boolean = alive && action.done && !maze.blockMovement((tx, ty), Move(d).destination, this)

  var action:Action = Idle()

  def alive = action != Die()

  def setAction(a:Action): Future[Unit] = {
    if (alive && action.done) {
      action = a
      a.future
    } else {
      Future.failed(new IllegalStateException("I'm sorry, I can't do that, Hal"))
    }
  }

  /** BlobGuards don't block movement. */
  override def blockMovement(from: (Direction, Direction), to: (Direction, Direction), by: Mob): Boolean = false

  def die():Unit = action match {
    case Die() => //
    case _ => action = Die()
  }

  /**
   * Paint this mob on the canvas
   *
   * @param layer
   * @param ctx
   */
  override def paintLayer(layer: Direction, x1: Direction, y1: Direction, x2: Direction, y2: Direction, ctx: CanvasRenderingContext2D): Unit = {
    action.paintLayer(layer, x1, y1, x2, y2, ctx)
  }

  override def tick(m:Maze) = {
    if (alive) {
      for {mob <- maze.mobsIntersecting(hitBox)} if (mob != this) {
        mob match {
          case s: Snobot => s.die()
          case _ => //
        }
      }
    }

    action.tick(m)
    if (action.done) {
      action match {
        case Die() => // Do nothing, it's stopped
        case _ => ai(m, this)
      }
    }
  }

}
