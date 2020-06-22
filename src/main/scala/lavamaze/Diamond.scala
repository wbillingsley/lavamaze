package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object Diamond {
  private val image = loadImage("diamond.png")

  val dimension = 56
  val offset = (oneTile - dimension) / 2

  def drawStatic(i:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, 64 * (i % 3), 0, 64, 64, offsetX + offset, offsetY + offset, dimension, dimension)
  }

}

case class Diamond(tx:Int, ty:Int) extends Fixture {

  val px:Int = tx * oneTile
  val py:Int = ty * oneTile

  val bounds:((Int,Int), (Int, Int)) = (px, py) -> (px + oneTile, py + oneTile)

  var t = 0
  /**
   * Paint this mob on the canvas
   *
   * @param layer
   * @param ctx
   */
  override def paintLayer(layer: Direction, x1: Direction, y1: Direction, x2: Direction, y2: Direction, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == 6 && intersects(bounds, (x1, y1) -> (x2, y2))) {
      Diamond.drawStatic(t / 8)(px - x1, py - x1, ctx)
    }
  }

  override def tick(maze: Maze): Unit = {
    t += 1

    for {mob <- maze.mobsIntersecting(bounds)} {
      mob match {
        case _: Snobot => maze.removeFixture(this)
        case _ => //
      }
    }
  }

  /** Whether this mob will block another mob's movement into a space */
  override def blockMovement(from: (Int, Int), to: (Int, Int), by: Mob): Boolean = by match {
    case _:Snobot => false
    case _ => (tx, ty) == to
  }
}
