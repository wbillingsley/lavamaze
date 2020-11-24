package lavasite.templates

import com.wbillingsley.veautiful.html.{<, VHtmlNode, ^}
import org.scalajs.dom
import org.scalajs.dom.{Event, Node, html}

import scala.scalajs.js
import scala.util.Random

/**
 * Ace.js seems to struggle with selection sometimes if it is within a page that has a CSS transform that scales
 * the edit area. This component supplies an Ace.js editor, but applies an inverse scale and adjusts the Ace.js
 * editor font-size so that it will appear visually the correct size but internally have a CSS scale close to 1.
 *
 * @param name the name of the component, used for equality checks in DiffComponent trees
 * @param fontSize the font size of the editor
 * @param onStart code to run on the editor when it is first loaded
 */
case class DescaledAceEditor(name:String, fontSize:Int)(onStart: js.Dynamic => Unit) extends VHtmlNode {

  val id = Random.nextString(10)

  private var _domNode:Option[html.Div] = None
  private var _editor:Option[js.Dynamic] = None

  override def domNode: Option[Node] = _domNode

  def editor = _editor

  var scale:Double = 1


  /**
   * registered to listen to rescale events
   */
  val rescaleEventListener: Event => Unit = (e:Event) => rescale()

  private def rescale():Unit = {
    for { n <- _domNode } {
      val r = n.getBoundingClientRect()
      val outerScale = Math.min(r.height / n.offsetHeight, r.width / n.offsetHeight)

      scale = 1 / outerScale
      n.childNodes(0).asInstanceOf[html.Div].style = s"transform-origin: 0 0; transform: scale($scale)"

      for {
        e <- editor
      } {
        val newPtSize = (fontSize * outerScale).toInt
        e.setFontSize(s"${newPtSize}px")
      }
    }
  }

  override def attach(): Node = {
    val n = <.div(^.attr("id") := id + "-wrapper", ^.cls := "ace-editor-wrapper").create()
    val u = <.div(^.cls := "ace-editor-unscaler").create()
    u.appendChild(<.div(^.attr("id") := id, ^.cls := "ace-editor").create())
    n.appendChild(u)
    _domNode = Some(n)
    n
  }

  override def afterAttach(): Unit = {
    super.afterAttach()

    import AceEditor.ace
    val editor = ace.edit(id)
    onStart(editor)
    _editor = Some(editor)

    dom.window.addEventListener("resize", rescaleEventListener)
    rescale()
  }

  override def beforeAttach(): Unit = {
    super.beforeAttach()
    dom.window.removeEventListener("resize", rescaleEventListener)
  }

  override def detach(): Unit = {


  }
}
