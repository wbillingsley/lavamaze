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
        r = LineBot(100, 100) { r =>
          r.lineSensors.append(new r.LineSensor(Vec2(50, 0)))
        },
        setup = c => {
          c.fillCanvas("white")
          c.withCanvasContext { ctx =>
            ctx.fillStyle = "rgb(255,127,60)"
            ctx.fillRect(100, 100, 300, 100)
          }
        }
      ))()
    ))
    .markdownSlide(Common.willCcBy).withClass("bottom")

  val deck = builder.renderNode


}
