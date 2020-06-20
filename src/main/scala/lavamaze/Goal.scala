package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object Goal {
  private val image = loadImage("goal.png")

  def drawStatic(i:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, 64 * (i % 4), 0, 64, 64, offsetX, offsetY, 64, 64)
  }

  def drawExit(i:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    if (i <= 4) {
      ctx.fillStyle = "rgba(255, 255, 255, 0.5)"
      ctx.fillRect(offsetX, offsetY, 64, 64)
    } else if (i > 4 && i <= 12) {
      ctx.drawImage(image, 64 * 0, 64, 64, 64, offsetX, offsetY, 64, 64)
    } else if (i > 4 && i <= 20) {
      ctx.drawImage(image, 64 * 1, 64, 64, 64, offsetX, offsetY, 64, 64)
    } else if (i > 4 && i <= 28) {
      ctx.drawImage(image, 64 * 2, 64, 64, 64, offsetX, offsetY, 64, 64)
    }
  }

}

case class Goal(tx:Int, ty:Int) extends Fixture {

  val x = tx * oneTile
  val y = ty * oneTile

  private var t = 0;

  private var snobotExited = false


  def intersects(x1:Int, y1:Int, x2:Int, y2:Int):Boolean = {
    inside(x, y)(x1, y1, x2, y2) ||
    inside(x1, y1)(x, y, x + oneTile, y + oneTile) ||
    inside(x2, y2)(x, y, x + oneTile, y + oneTile)
  }

  def intersects(bounds:((Int, Int), (Int, Int))):Boolean = {
    val ((x1, y1), (x2, y2)) = bounds
    intersects(x1, y1, x2, y2)
  }

  /**
   * Paint this mob on the canvas
   *
   * @param layer
   * @param ctx
   */
  override def paintLayer(layer: Direction, x1: Direction, y1: Direction, x2: Direction, y2: Direction, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == 6 && intersects(x1, y1, x2, y2)) {
      if (snobotExited) Goal.drawExit(t)(x, y, ctx) else Goal.drawStatic(t / 8)(x, y, ctx)
    }
  }

  def tick(maze: Maze): Unit = {
    t += 1

    if (maze.snobot.tx == tx && maze.snobot.ty == ty && maze.snobot.action == maze.snobot.Idle()) {
      t = 0
      snobotExited = true
      maze.snobot.setAction(maze.snobot.AtGoal())
    }
  }
}
