package lavamaze

import org.scalajs.dom

trait Mob {

  /**
   * Paint this mob on the canvas
   * @param layer
   * @param ctx
   */
  def paintLayer(layer:Int, x1:Int, y1:Int, x2:Int, y2:Int, ctx:dom.CanvasRenderingContext2D)

}
