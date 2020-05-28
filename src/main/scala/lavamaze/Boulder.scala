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

class Boulder(initTx:Int, initTy:Int) extends Mob {

  var px = initTx * oneTile
  var py = initTy * oneTile

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
    //
  }
}
