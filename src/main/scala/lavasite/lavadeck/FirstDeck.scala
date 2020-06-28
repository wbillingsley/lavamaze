package lavasite.lavadeck

import canvasland.willtap.imperativeTopic.Vec2
import canvasland.{CanvasLand, Turtle}
import coderunner.JSCodable
import com.wbillingsley.veautiful.html.<
import com.wbillingsley.veautiful.templates.DeckBuilder
import lavamaze.Maze
import lavasite.Common

object FirstDeck {

  val jsc = JSCodable(Maze()((10, 10), (10, 10)) { maze =>
    maze.loadFromString(
      """
        | #...O.Z
        | #.####*#
        | #S.>B..#
        | #.##.###
        | #.<d..G
        | #..v.v..
        | #.**.**.
        |""".stripMargin)
  })(tilesMode = false)

  val logo = JSCodable(CanvasLand()(
    r = Turtle(320, 320),
    setup = (c) => {
      c.drawGrid("lightgray", 25)
    }
  ))()

  val builder = new DeckBuilder()
    .markdownSlide(
      """
        |# The Lava Maze Test Deck
        |
        |A quick slide deck including some lava maze environments
        |
        |""".stripMargin).withClass("center middle")
    .veautifulSlide(<.div(
      <.h1("Welcome to the Lava Maze"),
      <.p(
        Maze()((1,1), (1,1)) { m => m.loadFromString("S"); m.start() },
        Common.markdown("*Snobot*: our hero")
      ),
      <.p(
        Maze()((1,1), (1,1)) { m => m.loadFromString(" S") },
        Common.markdown("*Lava*: deadly to snobots, passable to blob guards")
      ),
      <.p(
        Maze()((1,1), (1,1)) { m => m.loadFromString(".S") },
        Common.markdown("*Floor*: passable to snobots and blob guards")
      ),
      <.p(
        Maze()((1,1), (1,1)) { m => m.loadFromString("GS") },
        Common.markdown("*Goal*: where Snobot has to get to")
      ),
      <.p(
        Maze()((1,1), (1,1)) { m => m.loadFromString("*S") },
        Common.markdown("*Diamond*: collectable but slippery")
      ),
      <.p(
        Maze()((1,1), (1,1)) { m => m.loadFromString("OS") },
        Common.markdown("*Boulder*: you can push them around")
      ),
      <.p(
        Maze()((1,1), (1,1)) { m => m.loadFromString("BS") },
        Common.markdown("*Blob Guard*: our villain")
      )
    ))
    .veautifulSlide(
      <.div(
        <.h1("Maze"),
        jsc
      )
    )
    .veautifulSlide(
      <.div(
        <.h1("Maze"),
        JSCodable(
          Maze("Challenge1")((8, 8), (8, 8)) { maze =>
            maze.loadFromString(
              """
                |
                |  ...
                |S..#.ZG
                |  ...
                |
                |""".stripMargin)
          }
        )()
      )
    )
    .veautifulSlide(<.div(
      <.h2("Turtle graphics"),
      logo
    ))
    .markdownSlide(
      """
        |## To-do:
        |
        |Some test decks I need to make
        |
        |1. Goal square
        |2. Random snaking path
        |3. Spoiler paths
        |4. Overlays (treacle algorithm)
        |5. Reintroduce the blob guards
        |6. Doors, pressure plates, and keys
        |
        |""".stripMargin
    )
    .markdownSlide(Common.willCcBy).withClass("bottom")

  val deck = builder.renderNode


}
