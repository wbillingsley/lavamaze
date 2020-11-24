package canvasland

import coderunner.Codable
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.concurrent.{Future, Promise}

/** A robot travels the canvas land */
trait Robot extends Mob {

  def functions(): Seq[Codable.Triple]

}

object Robot {

  /** An Action that a robot can perform */
  trait Action {
    def done:Boolean = promise.isCompleted

    def tick(canvasLand: CanvasLand):Unit

    protected val promise:Promise[Unit] = Promise()

    def fail(x:Throwable): Unit = {
      promise.failure(x)
    }

    def future:Future[Unit] = promise.future
  }

}