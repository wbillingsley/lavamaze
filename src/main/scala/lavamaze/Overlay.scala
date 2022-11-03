package lavamaze

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.collection.mutable

trait Overlay {

  def reset(m:Maze):Unit = {  }

  def tick(m:Maze):Unit = {  }

  def paintTile(layer:Int, tx:Int, ty:Int, ctx:dom.CanvasRenderingContext2D):Unit

}

object Overlay {

  class CoordinateOverlay(m:Maze) extends Overlay {

    override def paintTile(layer: Direction, tx: Direction, ty: Direction, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == FLOOR_EFFECT) {
        ctx.fillStyle = "rgba(255,255,255,0.5)"
        ctx.textAlign = "center"
        ctx.textBaseline = "middle"
        ctx.font = "24px sans-serif"
        ctx.fillText(s"$tx,$ty", oneTile / 2, oneTile / 2)
      }
    }

  }

  class FloodFill() extends Overlay {

    val distanceMap = mutable.Map.empty[(Int, Int), Int]

    override def reset(m: Maze): Unit = tick(m)

    override def tick(m: Maze): Unit = {
      distanceMap.clear()

      def check(p:(Int, Int), dist:Int):Unit = {
        distanceMap(p) = dist
        val (x, y) = p

        for {
          (dx, dy) <- Seq((x+1, y), (x-1, y), (x, y+1), (x, y-1)) if (
            distanceMap.getOrElse((dx, dy), Int.MaxValue) > dist + 1 &&
              !m.blockMovement((dx, dy), p, m.snobot) &&
              m.getTile((dx, dy)).isPassableTo(m.snobot)
          )
        } check((dx, dy), dist + 1)
      }

      for { (x, y) <- m.getGoals } check((x, y), 0)
    }

    override def paintTile(layer: Direction, tx: Direction, ty: Direction, ctx: CanvasRenderingContext2D): Unit = {
      if (layer == FLOOR_EFFECT && distanceMap.contains(tx -> ty)) {
        ctx.fillStyle = "rgba(255,255,255,0.5)"
        ctx.textAlign = "center"
        ctx.textBaseline = "middle"
        ctx.font = "24px sans-serif"
        ctx.fillText(s"${distanceMap(tx -> ty)}", oneTile / 2, oneTile / 2)
      }
    }

  }



}
