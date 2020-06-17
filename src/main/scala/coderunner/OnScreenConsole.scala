package coderunner

import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom.{Element, Node, html}

import scala.scalajs.js

/**
 * A simple text area that can be programmatically written to.
 */
class OnScreenConsole extends VHtmlComponent {

  private var _text:String =
    """You can print to this text area using println("Hello").
      |You can also print to the browser's own "console" using console.log("Hello").
      |""".stripMargin

  def text:String = _text

  def clear():Unit = {
    _text = ""
    rerender()
  }

  def println(s:String):Unit = {
    _text = text + s + "\n"
    rerender()
  }

  override protected def render: DiffNode[Element, Node] = {
    <.textarea(^.cls := "onscreen-console", ^.attr("readonly") := "readonly", ^.prop("value") := _text)
  }
}
