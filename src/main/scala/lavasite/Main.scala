package lavasite

import com.wbillingsley.veautiful.html.Attacher
import org.scalajs.dom

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("LavaMazeSite")
object Main {

  val scaleChallengesToWindow:Boolean = {
    !dom.window.location.search.contains("scale=off")
  }

  @JSExport
  def load(): Unit = {
    val n = dom.document.getElementById("render-here")
    n.innerHTML = ""
    val root = Attacher.newRoot(n)
    root.render(Router)
  }

}
