package lavamaze

import org.scalajs.dom.CanvasRenderingContext2D

object FloorTile extends Tile {

  val floorFill = "rgb(20,20,20)"
  val floorStroke = "rgb(120,120,120)"
  val floorLayer = 1

  override def isPassableTo(m: Mob): Boolean = true

  override def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == floorLayer) {
      ctx.fillStyle = floorFill
      ctx.strokeStyle = floorStroke
      ctx.fillRect(x, y, oneTile, oneTile)
      ctx.strokeRect(x, y, oneTile, oneTile)
    }
  }

}
