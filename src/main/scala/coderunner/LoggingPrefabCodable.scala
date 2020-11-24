package coderunner

import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, ^}
import org.scalajs.dom.{Element, Node}

import scala.scalajs.js

case class LoggingPrefabCodable(code:String, codable:Codable, codeStyle:Option[String] = None, asyncify:Boolean = true) extends VHtmlComponent {

  val console = new OnScreenHtmlConsole(100)

  private val println:(String, js.Function) = "println" -> { x:Any => console.println(x.toString) }
  private val functions = (for { (n, _, f) <- codable.functions } yield n -> f).toMap + println

  private val codeRunner = new WorkerCodeRunner(
    functions,
    Map.empty, asyncify)

  val codePlayControls = CodePlayControls(codeRunner)(
    code,
    start = codable.start _,
    reset = () => { console.clear(); codable.reset() },
  )

  override protected def render: DiffNode[Element, Node] = <.div(^.cls := "jscodable",
    codable.vnode,
    console,
    <.pre(^.attr("style") ?= codeStyle, code),
    codePlayControls
  )

}
