package coderunner

import coderunner.ButtonDrawer.BDContent
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlElement, VHtmlContent, ^}
import org.scalajs.dom.{Element, Node}

case class ButtonDrawer(contents:BDContent*) extends VHtmlComponent {
  override protected def render = {
    <.div(^.cls := "button-drawer",
      contents map {
        case s:String => <.div(s)
        case n:VHtmlContent @unchecked => n
        case i:Iterable[VHtmlContent] @unchecked => <.div(^.cls := "btn-group-vertical", i)
      }
    )
  }
}


object ButtonDrawer {

  type BDContent = String | VHtmlContent | Iterable[VHtmlContent]

}
