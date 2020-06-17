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
    dom.console.log("Setting up maze")

    maze.loadFromString(
      """
        | #...O.Z
        | #.####*#
        | #S..B..#
        | #.##.###
        | #..d..G
        |""".stripMargin)
  }

  val rpcs = Map[String, js.Function](
    "ping" -> (() => println("ping")),
    "canGoRight" -> (() => m.snobot.canMove(lavamaze.EAST)),
    "right" -> (() => m.snobot.ask(Snobot.MoveMessage(lavamaze.EAST)).toJSPromise),
    "left" -> (() => m.snobot.ask(Snobot.MoveMessage(lavamaze.WEST)).toJSPromise),
    "up" -> (() => m.snobot.ask(Snobot.MoveMessage(lavamaze.NORTH)).toJSPromise),
    "down" -> (() => m.snobot.ask(Snobot.MoveMessage(lavamaze.SOUTH)).toJSPromise),
    "zing" -> { (i:Int, j:Int, k:Int) => println(s"Zinged with i=$i j=$j k=$j"); "Done" }
  )

  val cr = new WorkerCodeRunner(rpcs, Map.empty, true)


  val ace = AceEditor("mycode") { editor =>
    editor.setTheme("ace/theme/monokai")
    editor.setFontSize("24px")
    editor.setOption("hasCssTransforms", true)
    editor.session.setMode("ace/mode/javascript")
  }

  val cpc = CodePlayControls(cr)(ace.editor.map(_.getValue().asInstanceOf[String]).getOrElse(""), m.reset())

  val jsc = JSCodable()(m)()

  val scatterCanvas = new TileSpace(Some("example"), JSLang)((512, 640))
  val pt = new ProgramTile(scatterCanvas, <.button(^.cls := "btn btn-sm btn-primary", "Run"))
  pt.x = 2
  pt.y = 2

  val dt = new DeleteTile(scatterCanvas)
  dt.x = 420
  dt.y = 2

  scatterCanvas.tiles.appendAll(Seq(pt, dt))


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
       /* Challenge.split(
          <.div(
            m,
          )
        )(
          <.div(^.attr("style") := "position: relative; width: 600px; height: 300px;",
            ace,
          ),
          cpc
        ), */
      )
    )
    .veautifulSlide(<.div(
      <.h1("Using tiles"),
      Challenge.split(
        Maze()((10, 10), (10, 10)) { maze =>
          dom.console.log("Setting up maze")

          maze.loadFromString(
            """ ........
              | #S.....#
              | ######*#
              | #...B..#
              | #.##.###
              | #.....G
              |""".stripMargin)
        }
      )(
        scatterCanvas
      )
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
