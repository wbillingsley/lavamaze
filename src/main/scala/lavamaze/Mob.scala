package lavamaze

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.concurrent.{Future, Promise}

trait Mob {

  /**
   * Paint this mob on the canvas
   * @param layer
   * @param ctx
   */
  def paintLayer(layer:Int, x1:Int, y1:Int, x2:Int, y2:Int, ctx:dom.CanvasRenderingContext2D)

  def tick(m:Maze)

}

object Mob {

  /** An Action that Snobot can perform */
  trait Action {
    def done:Boolean = promise.isCompleted

    def paintLayer(layer: Int, x1: Int, y1: Int, x2:Int, y2:Int, ctx: CanvasRenderingContext2D): Unit

    def tick(maze:Maze):Unit

    protected val promise:Promise[Unit] = Promise()

    def fail(x:Throwable): Unit = {
      promise.failure(x)
    }

    def future:Future[Unit] = promise.future
  }


}