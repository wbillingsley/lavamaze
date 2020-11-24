package lavamaze

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

trait Tile {

  def isPassableTo(m:Mob):Boolean

  def isBlockingTo(m:Mob):Boolean

  def blockMovement(p:(Int, Int))(from:(Int, Int), to:(Int, Int), by:Mob):Boolean

  def paint(layer:Int, x:Int, y:Int, ctx:dom.CanvasRenderingContext2D)

  def actUpon(mob:Mob):Unit = {}

}


object Tile {

  object OutOfBounds extends Tile {
    override def isPassableTo(m: Mob): Boolean = false
    override def isBlockingTo(m: Mob): Boolean = true

    override def blockMovement(p:(Int, Int))(from: (Int, Int), to: (Int, Int), by: Mob): Boolean = {
      p.crossedBy(from, to)
    }

    override def paint(layer:Int, x:Int, y:Int, ctx: CanvasRenderingContext2D): Unit = {
      // do nothing
    }
  }

}