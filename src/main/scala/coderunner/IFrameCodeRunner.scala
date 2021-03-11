package coderunner

import coderunner.CodeRunner.RemoteParty
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom
import org.scalajs.dom.{Element, MessageEvent, Node, html}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.Random

object IFrameCodeRunner {


  /** The HTML that is loaded into an iframe used as the remote party */
  val iframeSrc:String = s"""
                   |<html>
                   |<script>
                   |console.log("iframe script running")
                   |
                   |let target = window.parent
                   |
                   |${WorkerCodeRunner.commonScript}
                   |
                   |window.addEventListener("message", messageHandler, false)
                   |
                   |</script>
                   |</html>
                   |""".stripMargin

  /**
   * We add listeners to our own map, because it is too difficult to try to remove listeners from
   * window.addEventListener. (Anonymous functions are not equal to each other, making anonymous listeners hard to
   * remove)
   */
  val listeners:mutable.Map[String, (MessageEvent) => _] = mutable.Map.empty
  dom.window.addEventListener("message", { (m:MessageEvent) =>
    dom.console.debug("Window received", m)
    listeners.values.foreach { l =>
      l(m)
    }
  })

  class IFrameRemote(iframe:html.IFrame) extends RemoteParty {

    val name:String = Random.nextString(10)

    override def setMessageHandler(f: Function[MessageEvent, _]): Unit = {
      IFrameCodeRunner.listeners(name) = f
    }

    override def postMessage(message: js.Any): Unit = {
      iframe.contentWindow.postMessage(message, "*")
    }

    override def terminate(): Unit = {
      if (IFrameCodeRunner.listeners.contains(name)) {
        IFrameCodeRunner.listeners.remove(name)
      }

      // Blank the iframe's src to remove the script
      iframe.src = "about:blank"
    }
  }

}

class IFrameCodeRunner(
  rpcs:Map[String, js.Function], bindings:Map[String, js.Any],
  awaitifyRpcs:Boolean = false
)(
  runningGraphic: => VHtmlNode, terminatedGraphic: => VHtmlNode,
) extends CodeRunner(rpcs, bindings, awaitifyRpcs) with VHtmlComponent {

  case class CodeRunnerFrame() extends VHtmlNode {
    var domNode: Option[html.IFrame] = None

    private val p = Promise[CodeRunner.RemoteParty]()
    def future = p.future

    override def attach(): Node = {
      val n = <.iframe(^.attr("srcdoc") := IFrameCodeRunner.iframeSrc).create()
      domNode = Some(n)
      n
    }

    override def afterAttach(): Unit = {
      super.afterAttach()
      for { iframe <- domNode } {
        val party = new IFrameCodeRunner.IFrameRemote(iframe)
        party.setMessageHandler( makeMessageHandler(
          onLoaded = () => {
            p.success(party)
          },
          destination = party
        ))
      }
    }

    override def beforeDetach(): Unit = {
      super.beforeDetach()
      for { remote <- p.future } remote.terminate()
    }

    override def detach(): Unit = {
      domNode = None
    }
  }

  var codeRunnerFrame:Option[CodeRunnerFrame] = None

  override protected def createWorker(): Future[CodeRunner.RemoteParty] = {
    val c = CodeRunnerFrame()
    codeRunnerFrame = Some(c)
    rerender()
    c.future
  }

  override protected def terminateWorker(): Unit = {
    codeRunnerFrame = None
    rerender()
  }

  override protected def render: DiffNode[Element, Node] = {
    <.div(
      codeRunnerFrame match {
        case Some(c) => Seq(runningGraphic, c)
        case _ => terminatedGraphic
      }
    )
  }

}
