package lavasite.templates

import com.wbillingsley.veautiful.html.{<, VHtmlElement, ^}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{Event, Node, html}

import scala.scalajs.js
import scala.util.Random

object AceEditor {
  // Reference to "ace" from the ace editor js
  def ace = js.Dynamic.global.ace
}

case class AceEditor(name:String)(onStart: js.Dynamic => Unit) extends VHtmlElement {

  val id = Random.nextString(10)

  private var _domNode:Option[html.Div] = None
  private var _editor:Option[js.Dynamic] = None

  override def domNode = _domNode

  def editor = _editor

  def value:String = editor.map(_.getValue().asInstanceOf[String]).getOrElse("")

  def insertAtCursor(s:String):Unit = {
    for { e <- editor } {
      e.session.insert(e.getCursorPosition(), s)
    }
  }

  override def attach() = {
    val n = <.div(^.attr("id") := id, ^.cls := "ace-editor").build().create()
    _domNode = Some(n)
    n
  }

  override def afterAttach(): Unit = {
    super.afterAttach()

    import AceEditor.ace
    val editor = ace.edit(id)
    onStart(editor)
    _editor = Some(editor)
  }

  override def detach(): Unit = {



  }
}
