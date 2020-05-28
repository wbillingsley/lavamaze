package lavamaze

import com.wbillingsley.veautiful.html.{<, ^}
import lavamaze.FloorTile.image
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object LavaTile extends Tile {

  val image = <("img")(^.src := "images/lava.png").create()

  override def isPassableTo(m: Mob): Boolean = false

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

}
