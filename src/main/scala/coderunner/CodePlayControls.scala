package coderunner

import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom.{Element, Node}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

case class CodePlayControls(codeRunner:CodeRunner)(code: => String, reset: => Unit, prependButtons: => Seq[VHtmlNode] = Seq.empty) extends VHtmlComponent {

  private val emptyMessage = <.span()
  private def error(s:String) = <.span(^.cls := "code-message text-danger", s)

  private val playBtn = <.button(^.key := "play", ^.cls := "btn btn-primary", ^.onClick --> play, <("i")(^.cls := "material-icons", "play_arrow"))
  private val stopBtn = <.button(^.key := "stop", ^.cls := "btn btn-primary", ^.onClick --> stop, <("i")(^.cls := "material-icons", "stop"))

  private var button:VHtmlNode = playBtn
  private var status:VHtmlNode = emptyMessage

  private def play():Unit = {
    reset
    status = emptyMessage
    button = stopBtn
    rerender()
    println("Run ")
    codeRunner.remoteExecute(code).andThen({
      case Success(x) =>
        println("Done " + x)
        button = playBtn
        rerender()
      case Failure(exception) =>
        println("Failed " + exception)
        status = error(exception.getMessage)
        button = playBtn
        rerender()
    })
  }

  private def stop():Unit = {
    codeRunner.reset()
    reset
    status = emptyMessage
    button = playBtn
    rerender()
  }

  override protected def render: DiffNode[Element, Node] = <.div(
    <.div(^.cls := "btn-group", ^.attr("role") := "group", prependButtons, button), status
  )
}
