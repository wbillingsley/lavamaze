package coderunner

import com.wbillingsley.veautiful.html.{VHtmlElement, VDomNode, VHtmlContent, VHtmlComponent, <, ^}
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

  def vnode:VDomNode

}


object Codable {

  type Triple = (String, Seq[String], js.Function)

  trait CanvasLike extends Codable with VHtmlElement {

    private var started = false

    def tickPeriod:Double

    def step():Unit

    def repaint():Unit

    def start(): Unit = {
      started = true
    }

    def stop():Unit = {
      started = false
    }

    var lastFrame:Double = 0
    override def afterAttach(): Unit = {
      dom.window.requestAnimationFrame(animationFrameHandler)
    }

    def animationFrameHandler(d:Double):Unit = {
      val ticks = ((d - lastFrame) / tickPeriod).toInt

      if (started) for { tick <- 0 until ticks } {
        step()
      }

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

case class StructureVis() extends VHtmlComponent with Codable {

  def vnode = this

  var visualise:List[(String, js.Any)] = Nil

  def show(name:String, j:js.Any):Unit = 
    visualise = (name, j) :: visualise
    rerender()

  def reset():Unit = 
    visualise = Nil
    rerender()

  def start():Unit = ()

  def functions() = Seq(("show", Seq("String", "Any"), (n, j) => show(n, j)))

  def renderJs(j:js.Any):VHtmlContent = j match {
    case arr:js.Array[js.Any] @unchecked => 
      <.div(^.cls := "js-array", arr.map(renderJs(_)))
    case f:js.Function => 
      <.span("func")
    case a => <.span(a.toString)
  }

  def render = <.div(
    for (n, js) <- visualise.toIterable yield {
      <.div(
        <.h4(n),
        <.p(
          renderJs(js)

        )
      )
    }
  )

}