package structures

import coderunner.Codable
import coderunner.Codable.Triple
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, DHtmlComponent, VHtmlElement}
import org.scalajs.dom.{Element, Node}

import scala.collection.mutable
import scala.scalajs.js

/**
 * Incomplete. ObjectInspector is intended (in future releases) to be a Codable that can visualise the state of a
 * JavaScript object.
 */
case class ObjectInspector() extends Codable {

  private val underInspection = mutable.Buffer.empty[js.Dynamic]

  private object renderComponent extends DHtmlComponent {
    override protected def render = <.div()
  }

  def addItem(item:js.Dynamic): Unit = {
    println("The type is " + item.getTypeOf)
  }

  override def reset(): Unit = {

  }

  override def start(): Unit = {

  }

  override def functions(): Seq[(String, Seq[String], js.Function)] = Seq[Triple](
    ("refresh", Seq.empty, () => { renderComponent.rerender() }),
    ("addItem", Seq("Any"), (x:js.Dynamic) => addItem(x) )
  )

  override def vnode = renderComponent
}
