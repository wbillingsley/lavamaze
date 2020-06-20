package lavamaze

import org.scalajs.dom

trait Fixture {

  /**
   * Paint this mob on the canvas
   * @param layer
   * @param ctx
   */
  def paintLayer(layer:Int, x1:Int, y1:Int, x2:Int, y2:Int, ctx:dom.CanvasRenderingContext2D):Unit

  def tick(maze: Maze):Unit

  def tx:Int

  def ty:Int

  /** Whether this mob will block another mob's movement into a space */
  def blockMovement(from:(Int, Int), to:(Int, Int), by:Mob):Boolean

}