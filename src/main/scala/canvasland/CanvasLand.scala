package canvasland

import java.awt.Robot

import coderunner.Codable
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom.{Element, Node}

import scala.scalajs.js

class CanvasLand(name:String = "canvasland")(
  viewSize:(Int, Int),
  fieldSize:(Int, Int)
)(r:Robot) extends Codable with VHtmlComponent {

  override def reset(): Unit = ???
  override def start(): Unit = ???

  override def functions(): Seq[(String, Seq[String], js.Function)] = ???

  override def vnode: VHtmlNode = this

  val bottomCanvas = <.canvas()
  val gridLayer = <.div(^.cls := "grid-layer")

  override protected def render: DiffNode[Element, Node] = {
    val (w, h) = viewSize

    <.div(^.cls := "canvasland", ^.attr("height") := s"${h}px", ^.attr("width") := s"${w}px",
      bottomCanvas,


    )
  }
}
