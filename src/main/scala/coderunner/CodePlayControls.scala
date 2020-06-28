package coderunner

import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom.{Element, Node}

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

case class CodePlayControls(codeRunner:CodeRunner)(
  code: => String,
  start: () => Unit = () => {}, reset: () => Unit, prependButtons: => Seq[VHtmlNode] = Seq.empty,
  onComplete: Try[Any] => Unit = { _ => }
) extends VHtmlComponent {

  private val emptyMessage = <.span(^.key := "empty")
  private def error(s:String) = <.span(^.cls := "code-message text-danger", s)

  private val playBtn = <.button(^.key := "play", ^.cls := "btn btn-primary", ^.onClick --> play,
    ^.attr("title") := "Run code", <("i")(^.cls := "material-icons", "play_arrow")
  )
  private val stopBtn = <.button(^.key := "stop", ^.cls := "btn btn-primary", ^.onClick --> stop,
    ^.attr("title") := "Reset", <("i")(^.cls := "material-icons", "replay")
  )

  private var button:VHtmlNode = playBtn
  private var status:VHtmlNode = emptyMessage

  private def play():Unit = {
    status = emptyMessage
    start()
    button = stopBtn
    rerender()

    codeRunner.remoteExecute(code).andThen({
      case Success(x) =>
        onComplete(Success(x))
        rerender()
      case Failure(exception) =>
        status = error(exception.getMessage)
        onComplete(Failure(exception))
        rerender()
    })
  }

  private def stop():Unit = {
    codeRunner.reset()
    reset()
    status = emptyMessage
    button = playBtn
    rerender()
  }

  override protected def render: DiffNode[Element, Node] = <.div(
    <.div(^.cls := "btn-group", ^.attr("role") := "group", prependButtons, button), status
  )
}
