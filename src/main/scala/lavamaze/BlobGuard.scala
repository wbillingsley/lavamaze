package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object BlobGuard {
  val image = <("img")(^.src := "images/blobguard.png").create()

  val dimension = 56
  val offset = (oneTile - dimension) / 2

  def drawImage(ix:Int, iy:Int)(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D):Unit = {
    ctx.drawImage(image, ix, iy, 64, 64, offsetX + offset, offsetY + offset, dimension, dimension)
  }

  def drawIdle(i:Int, x:Int, y:Int, ctx:dom.CanvasRenderingContext2D) = {
    if (i % 2 == 0) drawImage(2 * 64, 0)(x, y, ctx)
    else if (i % 4 == 1) drawImage(3 * 64, 0)(x, y, ctx)
    else drawImage(4 * 64, 0)(x, y, ctx)
  }
}

class BlobGuard(initTx:Int, initTy:Int) extends Mob {

  var px = initTx * oneTile
  var py = initTy * oneTile

  sealed trait Action extends Mob.Action

  /** The do nothing action */
  case class Idle() extends Action {
    var t = 0
    promise.success()

    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == 4) {
        val state = (t / 8) % 4
        BlobGuard.drawIdle(state, px -x1, py - y1, ctx)
      }
    }

    def tick(m:Maze): Unit = { t = t + 1 }
  }


  val state = Idle()

  /**
   * Paint this mob on the canvas
   *
   * @param layer
   * @param ctx
   */
  override def paintLayer(layer: Direction, x1: Direction, y1: Direction, x2: Direction, y2: Direction, ctx: CanvasRenderingContext2D): Unit = {
    state.paintLayer(layer, x1, y1, x2, y2, ctx)
  }

  override def tick(m:Maze) = state.tick(m)

}
