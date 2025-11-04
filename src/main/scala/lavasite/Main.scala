package lavasite

import com.wbillingsley.veautiful.doctacular.Site
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._
import com.wbillingsley.veautiful.html.{Markup, <, Styling, StyleSuite}
import lavasite.lavadeck.*

// This site loads Marked as its markdown parser directly from a script included in the page.
@js.native
@JSGlobal("marked")
object Marked extends js.Object:
  def parse(s:String):String = js.native

given markdown:Markup = new Markup({ (s:String) => Marked.parse(s).asInstanceOf[String] })
given styleSuite:StyleSuite = StyleSuite()
val site = Site()

@JSExportTopLevel("LavaMazeSite")
object Main {

  @JSExport
  def load(): Unit = {
    import site.given

    site.home = () => site.renderPage(intro)

    site.toc = site.Toc(
      "Home" -> site.HomeRoute,

      "Challenges" -> site.Toc(
        "Jul 2024 hands-on workshop" -> site.addChallenge("snobot-lava-maze", SnobotChallenge.levels ++ LanderChallenge.levels),
        "Oct 2023 interactive lecture" -> site.addChallenge("oct-2023", trythisathome.levels),
        "Oct 2024 interactive demo" -> site.addChallenge("oct-2024", trythisathome.levels2024),
        "Nov 2025 interactive demo" -> site.addChallenge("nov-2025", trythisathome.levels2025),
      ),

    )

    styleSuite.install()
    site.attachToBody()
  }

}
