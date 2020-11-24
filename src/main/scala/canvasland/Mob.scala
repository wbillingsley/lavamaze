package canvasland

import coderunner.Codable
import org.scalajs.dom

/**
 * In CanvasLand, Mobs are items that need rendering onto the canvas and can also be stepped.
 */
trait Mob extends Steppable {

  def x:Double

  def y:Double

  def draw(ctx:dom.CanvasRenderingContext2D):Unit

  def reset():Unit

}
