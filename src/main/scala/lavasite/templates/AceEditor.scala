package lavasite.templates

import com.wbillingsley.veautiful.html.{<, VHtmlNode, ^}
import org.scalajs.dom.Node

import scala.scalajs.js
import scala.util.Random

object AceEditor {
  // Reference to "ace" from the ace editor js
  def ace = js.Dynamic.global.ace
}

case class AceEditor(name:String)(onStart: js.Dynamic => Unit) extends VHtmlNode {

  val id = Random.nextString(10)

  private var _domNode:Option[Node] = None
  private var _editor:Option[js.Dynamic] = None

  override def domNode: Option[Node] = _domNode

  def editor = _editor

  override def attach(): Node = {
    val n = <.div(^.attr("id") := id, ^.cls := "ace-editor").create()
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
