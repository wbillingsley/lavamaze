package lavasite.lavadeck

import canvasland.{CanvasLand, LineBot, Turtle, Vec2}
import coderunner.JSCodable
import com.wbillingsley.veautiful.html.<
import com.wbillingsley.veautiful.templates.DeckBuilder
import lavamaze.{Gate, Maze, Overlay}
import lavasite.Common
import structures.ObjectInspector

object LineBotDeck {

  val builder = new DeckBuilder()
    .markdownSlide(
      """
        |# LineBot Test Deck
        |
        |A quick slide deck including some test environments for LineBot
        |
        |""".stripMargin).withClass("center middle")
    .veautifulSlide(<.div(
      <.h1("LineBot"),
      JSCodable(CanvasLand()(
        fieldSize=(1000 -> 1000),
        r = LineBot(150, 100) { r => },
        setup = c => {
          c.fillCanvas("rgb(200,180,0)")
          c.drawGrid("rgb(200,240,240)", 25, 1)
          c.withCanvasContext { ctx =>
            ctx.fillStyle = "rgb(60,60,60)"
            ctx.fillRect(50, 100, 900, 100)
          }
        }
      ))()
    ))
    .markdownSlide(Common.willCcBy).withClass("bottom")

  val deck = builder.renderNode


}
