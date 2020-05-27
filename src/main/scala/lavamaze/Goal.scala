package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object Goal {
  val image = <("img")(^.src := "images/goal.png").create()

  def drawStatic(i:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, 64 * (i % 4), 0, 64, 64, offsetX, offsetY, 64, 64)
  }

  def drawExit(i:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, 64 * (i % 3), 64, 64, 64, offsetX, offsetY, 64, 64)
  }

}

case class Goal(tx:Int, ty:Int) extends Fixture {

  private var t = 0;

  /**
   * Paint this mob on the canvas
   *
   * @param layer
   * @param ctx
   */
  override def paintLayer(layer: Direction, x1: Direction, y1: Direction, x2: Direction, y2: Direction, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == 6) {
      Goal.drawStatic(t / 8)(tx, ty, ctx)
    }

  }

  def tick(): Unit = {
    t += 1
  }
}
