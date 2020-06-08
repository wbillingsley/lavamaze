package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D


object Boulder {
  val image = <("img")(^.src := "images/boulder.png").create()

  val dimension = 56
  val offset = (oneTile - dimension) / 2

  def drawStatic(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, 0, 0, 64, 64, offsetX + offset, offsetY + offset, dimension, dimension)
  }
}

class Boulder(maze:Maze, initTx:Int, initTy:Int) extends GridMob {

  var px = initTx * oneTile
  var py = initTy * oneTile

  def hitBox = ((px + 4, py + 4), (px + oneTile - 4, py + oneTile - 4))

  var gravity:Option[Direction] = None

  def tx = px / oneTile
  def ty = py / oneTile

  override def blockMovement(from: (Direction, Direction), to: (Direction, Direction), by: Mob): Boolean = {
    action match {
      case m:Move =>
        m.destination == to || (m.destination == from && m.origin == to)
      case a =>
      a.destination == to
    }
  }

  sealed trait Action extends GridAction

  case class Idle() extends GridIdle() with Action   {
    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      paintLayer(layer, x1, y1, x2, y2, ctx)
    }
  }

  case class Move(d:Direction) extends GridMove(d, 8) with Action  {
    override def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      paintLayer(layer, x1, y1, x2, y2, ctx)
    }

    override def tick(m:Maze):Unit = {
      super.tick(m)
      for { mob <- m.mobsIntersecting(hitBox) } mob match {
        case s:Snobot => s.die()
        case b:BlobGuard => b.die()
        case _ => //

      }

    }
  }


  var action:Action = Idle()

  /**
   * Paint this mob on the canvas
   *
   * @param layer
   * @param ctx
   */
  override def paintLayer(layer: Direction, x1: Direction, y1: Direction, x2: Direction, y2: Direction, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == MOB_LOW) {
      Boulder.drawStatic(px - x1, py - y1, ctx)
    }
  }

  override def tick(m:Maze) = {
    if (action.done) {
      action = nextAction()

    } else action.tick(m)
  }

  def nextAction():Action = action match {
    case Idle() => action
    case _ => Idle()
  }

  def push(d:Direction):Boolean = {
    val a = Move(d)
    if (maze.blockMovement((tx, ty), a.destination, this)) {
      false
    } else {
      action = a
      true
    }
  }
}
