package lavasite

import com.wbillingsley.veautiful.html.Attacher
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._
import com.wbillingsley.veautiful.html.Markup


// This site loads Marked as its markdown parser directly from a script included in the page.
@js.native
@JSGlobal("marked")
object Marked extends js.Object:
  def parse(s:String):String = js.native

given markdown:Markup = new Markup({ (s:String) => Marked.parse(s).asInstanceOf[String] })

@JSExportTopLevel("LavaMazeSite")
object Main {

  @JSExport
  def load(): Unit = {
    val n = dom.document.getElementById("render-here")
    n.innerHTML = ""
    val root = Attacher.newRoot(n)
    root.render(Router)
  }

}
