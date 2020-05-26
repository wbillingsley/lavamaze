package lavamaze

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object LavaTile extends Tile {

  override def isPassableTo(m: Mob): Boolean = false

  override def paint(layer: Direction, x: Direction, y: Direction, ctx: CanvasRenderingContext2D): Unit = {
    // Do nothing, just let the background show through
  }

}
