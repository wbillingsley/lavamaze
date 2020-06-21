package lavasite.lavadeck

import coderunner.{ButtonDrawer, CodePlayControls, CodeRunner, IFrameCodeRunner, JSCodable, WorkerCodeRunner}
import com.wbillingsley.scatter.TileSpace
import com.wbillingsley.scatter.jstiles.{JSLang, ProgramTile}
import com.wbillingsley.veautiful.{DiffNode, MutableArrayComponent}
import com.wbillingsley.veautiful.html.{<, SVG, VHtmlComponent, VHtmlNode, ^}
import com.wbillingsley.veautiful.templates.{Challenge, DeckBuilder}
import jstiles.lavamaze.DeleteTile
import lavamaze.{BlobGuard, Boulder, Diamond, FloorTile, Goal, Maze, Snobot}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node, html, svg}
import lavasite.Common
import lavasite.templates.{AceEditor, DescaledAceEditor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}

object FirstDeck {

  import scala.scalajs.js.JSConverters._

  val m = Maze()((10, 10), (10, 10)) { maze =>
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
  }



  val jsc = JSCodable(m)()


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
        Maze()((1,1), (1,1)) { m => m.loadFromString("S") },
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
