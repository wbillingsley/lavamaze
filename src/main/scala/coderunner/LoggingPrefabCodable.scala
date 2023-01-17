package coderunner

import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, DHtmlComponent, ^}
import org.scalajs.dom.{Element, Node}

import scala.scalajs.js
import com.wbillingsley.veautiful.html.Styling

/** 
 * A codable containing some pre-fabricated code and a logging area.
 */
case class LoggingPrefabCodable(code:String, codable:Codable, codeStyle:Option[String] = None, asyncify:Boolean = true) extends DHtmlComponent {

  val console = new OnScreenHtmlConsole(100)

  private val println:(String, js.Function) = "println" -> { (x:Any) => console.println(x.toString) }
  private val functions = (for { (n, _, f) <- codable.functions() } yield n -> f).toMap + println

  private val codeRunner = new WorkerCodeRunner(
    functions,
    Map.empty, asyncify)

  val codePlayControls = CodePlayControls(codeRunner)(
    code,
    start = codable.start _,
    reset = () => { console.clear(); codable.reset() },
  )

  override protected def render = <.div(^.cls := "jscodable",
    codable.vnode,
    console,
    <.pre(^.attr("style") ?= codeStyle, code),
    codePlayControls
  )

}


/** 
 * A codable containing some pre-fabricated code.
 */
case class PrefabCodable(code:String, codable:Codable, codeStyle:Option[String] = None, asyncify:Boolean = true) extends DHtmlComponent {

  private val codeRunner = new WorkerCodeRunner(
    ((for { (n, _, f) <- codable.functions() } yield n -> f).toMap),
    Map.empty, asyncify)

  val codePlayControls = CodePlayControls(codeRunner)(
    code,
    start = codable.start _,
    reset = codable.reset _,
  )

  override protected def render = <.div(
    codable.vnode,
    <.pre(^.attr("style") ?= codeStyle, code),
    codePlayControls
  )

}

case class DynamicPrefabCodable(name:String)(code: => String, codable:Codable, codeClass:Seq[String | Styling | Unit] = Seq.empty, codeStyle:Option[String] = None, asyncify:Boolean = true) extends DHtmlComponent {

  private val codeRunner = new WorkerCodeRunner(
    ((for { (n, _, f) <- codable.functions() } yield n -> f).toMap),
    Map.empty, asyncify)

  val codePlayControls = CodePlayControls(codeRunner)(
    code,
    start = codable.start _,
    reset = codable.reset _,
  )

  override protected def render = <.div(
    codable.vnode,
    <.pre(^.cls.:=(codeClass*), ^.attr.style ?= codeStyle, code),
    codePlayControls
  )

}