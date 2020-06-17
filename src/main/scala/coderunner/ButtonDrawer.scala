package coderunner

import coderunner.ButtonDrawer.BDContent
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom.{Element, Node}

case class ButtonDrawer(contents:BDContent*) extends VHtmlComponent {
  override protected def render: DiffNode[Element, Node] = {
    <.div(^.cls := "button-drawer",
      contents map {
        case s:ButtonDrawer.StringWrapper => <.div(s.s)
        case n:ButtonDrawer.NodeWrapper => n.d
        case i:ButtonDrawer.IterableWrapper => <.div(^.cls := "btn-group-vertical", i.d)
      }
    )

  }
}


object ButtonDrawer {

  sealed trait BDContent
  implicit class StringWrapper(val s:String) extends BDContent
  implicit class NodeWrapper(val d:VHtmlNode) extends BDContent
  implicit class IterableWrapper(val d:Iterable[VHtmlNode]) extends BDContent



}
