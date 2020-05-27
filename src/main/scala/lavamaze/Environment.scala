package lavamaze

import org.scalajs.dom

/**
 * The environment is the kind of level we're in. For LavaMaze, it's mostly lava.
 */
trait Environment {

  def defaultTile:Tile

  def paintLayer(layer:Int, x1:Int, y1:Int, x2:Int, y2:Int, ctx:dom.CanvasRenderingContext2D)

  def tick():Unit

}
