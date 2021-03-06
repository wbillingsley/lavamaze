package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import org.scalajs.dom.CanvasRenderingContext2D

object FloorTile extends Tile {

  private val image = loadImage("floor.png")

  val floorFill = "rgb(20,20,20)"
  val floorStroke = "rgb(120,120,120)"
  val floorLayer = 3

  override def isPassableTo(m: Mob): Boolean = true
  override def isBlockingTo(m: Mob): Boolean = false

  override def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == floorLayer) {
      /*
      ctx.fillStyle = floorFill
      ctx.strokeStyle = floorStroke
      ctx.fillRect(x, y, oneTile, oneTile)
      ctx.strokeRect(x, y, oneTile, oneTile)
      */
      ctx.drawImage(image, 0, 0, 64, 64, x, y, 64, 64)
    }
  }

  override def blockMovement(p:(Int, Int))(from: (Int, Int), to: (Int, Int), by: Mob): Boolean = false
}
