package coderunner


import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlElement, ^}
import org.scalajs.dom.{Element, Node, html}

import scala.collection.mutable
import scala.scalajs.js

/**
 * A simple text area that can be programmatically written to.
 */
class OnScreenHtmlConsole(height: Int) extends VHtmlComponent {

  private val logEntries:mutable.Buffer[OnScreenHtmlConsole.LogEntry] = mutable.Buffer(
    OnScreenHtmlConsole.StyledEntry(
      """You can print to this text area using println("Hello").
        |You can also print to the browser's own "console" using console.log("Hello").
        |""".stripMargin, "comment")
  )


  def clear():Unit = {
    logEntries.clear()
    rerender()
  }

  def println(s:String):Unit = {
    logEntries.append(OnScreenHtmlConsole.StringEntry(s))
    rerender()
  }

  def printlnStyled(s:String, style:String):Unit = {
    logEntries.append(OnScreenHtmlConsole.StyledEntry(s, style))
    rerender()
  }


  private val heightStyle = s"height: ${height}px;"

  override protected def render = {
    <.div(^.cls := "onscreen-html-console", ^.attr("style") := heightStyle,
      for { e <- logEntries.toIterable } yield e match {
        case OnScreenHtmlConsole.StringEntry(s) => <.pre(s)
        case OnScreenHtmlConsole.StyledEntry(s, c) => <.div(^.cls := c, s)
      }
    )
  }
}

object OnScreenHtmlConsole {

  sealed trait LogEntry
  case class StringEntry(s:String) extends LogEntry
  case class StyledEntry(s:String, cls:String) extends LogEntry


}
