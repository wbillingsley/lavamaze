package lavasite

import com.wbillingsley.veautiful.doctacular.Site
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._
import com.wbillingsley.veautiful.html.{Markup, <}
import lavasite.lavadeck.{FirstDeck, LineBotDeck}

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
    val site = Site()
    import site.given

    site.home = () => site.renderPage(intro)

    site.toc = site.Toc(
      "Home" -> site.HomeRoute,

      "Codables" -> site.Toc(
        
      ),

      "Turtle" -> site.Toc(
        
      ),

      "Snobot" -> site.Toc(
        "Intro" -> site.addPage("snobot-intro", snobot)
        
      ),

      "LineBot" -> site.Toc(
        
      ),

      "Bumper" -> site.Toc(
        
      ),

      "Lander" -> site.Toc(
        
      ),

      "Test decks" -> site.Toc(
        "fd" -> site.addDeck("first-test-deck", FirstDeck.deck),
        "lbtd" -> site.addDeck("linebot-test-deck", LineBotDeck.deck),
      ),


    )

    site.attachToBody()
  }

}
