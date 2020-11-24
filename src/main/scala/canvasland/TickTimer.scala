package canvasland

import scala.concurrent.{Future, Promise}

/** Waits a number of ticks and then completes a promise */
case class TickTimer(howMany:Int) {
  var count = 0

  private val promise = Promise[Unit]

  def future:Future[Unit] = promise.future

  def complete:Boolean = promise.isCompleted

  def tick(): Unit = {
    count = count + 1
    if (count >= howMany) {
      promise.success(())
    }
  }

  def interrupt(): Unit = {
    if (!complete) {
      promise.failure(new InterruptedException("Interrupted"))
    }
  }

}
