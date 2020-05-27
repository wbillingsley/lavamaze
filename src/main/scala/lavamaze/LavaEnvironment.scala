package lavamaze

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

case class LavaEnvironment(dimensions:(Int, Int)) extends Environment {

  val fillStyle = "rgb(100,0,0)"
  val lavaBlobStyle = "rgb(115,0,0)"
  val lavaBlobStroke = "rgb(110,0,0)"

  def defaultTile = LavaTile

  case class LavaBlob(w:Double, h:Double, var x:Double = 0, var y:Double = 0, var r:Double = 0) {
    def reset(): Unit = {
      x = Math.random() * w
      y = Math.random() * h
      r = Math.random() * 50
    }

    def tick():Unit = {
      if (r < 1) {
        reset()
      } else {
        r = r * (19/20.0)
      }
      x = x + 0.1
      y = y + 0.1
    }

    def draw(offsetX:Int, offsetY:Int, ctx:dom.CanvasRenderingContext2D): Unit = {

      val xx = x + offsetX
      val yy = y + offsetY

      ctx.strokeStyle = lavaBlobStroke
      ctx.fillStyle = lavaBlobStyle
      ctx.beginPath()
      ctx.moveTo(xx, yy)
      ctx.arc(xx, yy, r, 0, Math.PI * 2)
      ctx.stroke()
      ctx.fill()
    }

    def visibleIn(x1:Int, y1:Int, x2:Int, y2:Int):Boolean = {
      x + r > x1 && x - r < x2 &&
      y + r > y1 && y - r < y2
    }
  }

  private val (width, height) = dimensions
  private val lavaBlobs = Array.fill(width * height / 2)(LavaBlob(width * oneTile, height * oneTile))

  override def paintLayer(layer: Int, x1:Int, y1:Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == 1) {
      ctx.fillStyle = fillStyle
      ctx.fillRect(0, 0, x2 - x1, y2 - y1)

      for {
        b <- lavaBlobs if b.visibleIn(x1, y1, x2, y2)
      } b.draw(x1, y1, ctx)
    }
  }

  override def tick(): Unit = {
    lavaBlobs.foreach(_.tick())
  }
}
