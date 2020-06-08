package lavamaze

import org.scalajs.dom.CanvasRenderingContext2D

/** Some mobs, e.g. Snobot and Boulders, mostly move from grid location to grid location */
trait GridMob extends Mob {

  def tx:Int
  def ty:Int

  def px_=(d:Int):Unit
  def py_=(d:Int):Unit

  trait GridAction extends Mob.Action {
    def origin:(Int, Int)

    /** The mob's expected tile coordinate when the action is complete */
    def destination:(Int, Int)
  }

  abstract class GridIdle() extends GridAction {
    var t = 0
    val origX:Int = tx
    val origY:Int = ty

    promise.success()

    override def origin = (origX, origY)
    override def destination: (Direction, Direction) = (tx, ty)

    def tick(m:Maze): Unit = { t = t + 1 }
  }

  abstract class GridMove(d:Direction, moveDuration:Int) extends GridAction {
    var t = 0
    val origX:Int = tx
    val origY:Int = ty

    val moveDistance:Int = oneTile / moveDuration

    override def origin = (origX, origY)

    override def destination: (Direction, Direction) = {
      d match {
        case EAST => (origX + 1, origY)
        case WEST => (origX - 1, origY)
        case SOUTH => (origX, origY + 1)
        case NORTH => (origX, origY - 1)
      }
    }

    override def tick(m:Maze): Unit = {
      t = t + 1
      d match {
        case EAST => px += moveDistance
        case WEST => px -= moveDistance
        case NORTH => py -= moveDistance
        case SOUTH => py += moveDistance
      }

      if (t >= moveDuration) promise.success()
    }
  }

}
