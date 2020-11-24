package lavamaze

import org.scalajs.dom

trait Drawable {

  def paintLayer(i:Int, ctx:dom.CanvasRenderingContext2D)

}
