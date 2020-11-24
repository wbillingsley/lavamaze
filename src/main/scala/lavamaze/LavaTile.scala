package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import lavamaze.FloorTile.image
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object LavaTile extends Tile {

  private val image = loadImage("lava.png")

  override def isPassableTo(m: Mob): Boolean = m match {
    case _:Snobot => false
    case _ => true
  }
  override def isBlockingTo(m: Mob): Boolean = false

  override def paint(layer: Direction, x: Direction, y: Direction, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == 1) {
      /*
      ctx.fillStyle = floorFill
      ctx.strokeStyle = floorStroke
      ctx.fillRect(x, y, oneTile, oneTile)
      ctx.strokeRect(x, y, oneTile, oneTile)
      */
      ctx.drawImage(image, 0, 0, 64, 64, x, y, 64, 64)
    }
  }

  override def actUpon(mob: Mob): Unit = {
    super.actUpon(mob)
    mob match {
      case s:Snobot => s.die()
    }
  }

  override def blockMovement(p:(Int, Int))(from: (Int, Int), to: (Int, Int), by: Mob): Boolean = false

}
