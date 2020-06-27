package coderunner

import com.wbillingsley.veautiful.html.VHtmlNode
import org.scalajs.dom

import scala.scalajs.js

/**
 * Something that can be included in a JSCodable
 */
trait Codable {

  // Puts the Codable back in its start state
  def reset():Unit

  // Starts the Codable ticking
  def start():Unit

  def functions():Seq[Codable.Triple]

  def vnode:VHtmlNode

}


object Codable {

  type Triple = (String, Seq[String], js.Function)

  trait CanvasLike extends Codable with VHtmlNode {

    private var started = false

    def tickPeriod:Double

    def step():Unit

    def repaint():Unit

    def start(): Unit = {
      started = true
    }

    var lastFrame:Double = 0
    override def afterAttach(): Unit = {
      dom.window.requestAnimationFrame(animationFrameHandler)
    }

    def animationFrameHandler(d:Double):Unit = {
      val ticks = ((d - lastFrame) / tickPeriod).toInt
      for { tick <- 0 until ticks } if (started) step()
      if (ticks > 0) {
        lastFrame = d
        try {
          repaint()
        } catch {
          case x:Throwable => dom.console.error(x.getMessage)
        }
      }

      if (isAttached) {
        dom.window.requestAnimationFrame(animationFrameHandler)
      }
    }
  }

}