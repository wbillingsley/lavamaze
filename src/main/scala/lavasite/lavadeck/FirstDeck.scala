package lavasite.lavadeck

import canvasland.{CanvasLand, Turtle}
import coderunner.{JSCodable, LoggingPrefabCodable}
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, ^}
import com.wbillingsley.veautiful.templates.{Challenge, DeckBuilder}
import lavamaze.{BlobGuard, Boulder, Dogbot, Gate, GridMob, Maze, Mob, Overlay, Snobot}
import lavasite.Common
import org.scalajs.dom.{Element, Node}
import structures.ObjectInspector

object FirstDeck {


  /** Components for the slide that shows the state of blobguards as they move through the maze */
  object ShowingState {

    val maze = Maze()((10, 5), (10, 5)) { maze =>
      maze.loadFromString(
        """ #v..vv.#
          | #S****G#
          | #.#...##
          | #Z..*..Z""".stripMargin)
      maze.additionalFunctions = maze.dogbotFunctions
    }

    private def stateCard(m:Mob) = m match {
      case s:Snobot =>
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "Snobot"),
          <.ul(^.cls := "list-group list-group-flush",
            <.li(^.cls := "list-group-item", ^.attr("style") := "margin-bottom: 0",
              s"x: ${s.px.toString}, y: ${s.py.toString}"
            ),
            <.li(^.cls := "list-group-item list-group-flush", ^.attr("style") := "margin-bottom: 0",
              s.action.stringify
            ),
            <.li(^.cls := "list-group-item list-group-flush",
              s.action.durationStringify
            )
          )
        )
      case s:BlobGuard =>
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "Blob Guard"),
          <.ul(^.cls := "list-group list-group-flush",
            <.li(^.cls := "list-group-item", ^.attr("style") := "margin-bottom: 0",
              s"x: ${s.px.toString}, y: ${s.py.toString}"
            ),
            <.li(^.cls := "list-group-item list-group-flush", ^.attr("style") := "margin-bottom: 0",
              s.action.stringify
            ),
            <.li(^.cls := "list-group-item list-group-flush",
              s.action.durationStringify
            )
          )
        )
      case s:Dogbot =>
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "Dogbot"),
          <.ul(^.cls := "list-group list-group-flush",
            <.li(^.cls := "list-group-item", ^.attr("style") := "margin-bottom: 0",
              s"x: ${s.px.toString}, y: ${s.py.toString}"
            ),
            <.li(^.cls := "list-group-item list-group-flush", ^.attr("style") := "margin-bottom: 0",
              s.action.stringify
            ),
            <.li(^.cls := "list-group-item list-group-flush",
              s.action.durationStringify
            )
          )
        )
      case s:Boulder =>
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "Boulder"),
          <.ul(^.cls := "list-group list-group-flush",
            <.li(^.cls := "list-group-item", ^.attr("style") := "margin-bottom: 0",
              s"x: ${s.px.toString}, y: ${s.py.toString}"
            ),
            <.li(^.cls := "list-group-item list-group-flush", ^.attr("style") := "margin-bottom: 0",
              s.action.stringify
            ),
            <.li(^.cls := "list-group-item list-group-flush",
              s.action.durationStringify
            )
          )
        )
      case _ =>
        <.span()
    }

    object StateItems extends VHtmlComponent {
      override def render: DiffNode[Element, Node] = <.div(
        for { m <- maze.allMobs } yield stateCard(m)
      )
    }

    maze.onTick = (_) => StateItems.rerender()

    val code =
      """while (canGoRight()) {
        |  right()
        |}
        |""".stripMargin

    val lfc = LoggingPrefabCodable(code, maze)

    def slide = <.div(
      <.h2("Showing state"),
      Challenge.textAndEx(
        <.div(^.cls := "card-columns",
          StateItems
        )
      )(lfc)
    )
  }

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
    maze.additionalFunctions = maze.dogbotFunctions
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
        <.h1("Dogbot"),
        JSCodable(
          Maze("Dogbot is in the maze!")((8, 8), (8, 8)) { maze =>
            maze.loadFromString(
              """#####*##
                |#####.#G
                |Z..S...1
                |####.###
                |Z..d...#
                |########
                |""".stripMargin)
            maze.additionalFunctions = maze.dogbotFunctions
          }
        )(tilesMode = false, asyncify = true)
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
        )(tilesMode = false)
      )
    )
    .veautifulSlide(ShowingState.slide)
    .veautifulSlide(
      <.div(
        <.h1("Maze"),
        JSCodable(
          Maze("Challenge1")((8, 8), (8, 8)) { maze =>
            maze.loadFromString(
              """
                |
                | Z.S.*...
                |     *< G
                |     *
                |
                |""".stripMargin)
            maze.addOverlay(new Overlay.CoordinateOverlay(maze))
            maze.getOverlays.foreach(_.tick(maze))
          }
        )(tilesMode = false)
      )
    )
    .veautifulSlide(
      <.div(
        <.h1("Maze"),
        JSCodable(
          Maze("Challenge1")((10, 8), (10, 8)) { maze =>
            maze.loadFromString(
              """
                |
                | Z.S.*.1.
                |     *< G
                |     *.
                |
                |""".stripMargin)
            maze.addOverlay(new Overlay.FloodFill())

          }
        )(tilesMode = false)
      )
    )
    .veautifulSlide(<.div(
      <.h2("Turtle graphics"),
      logo
    ))
    .veautifulSlide(
      <.div(
        <.h1("Type inspector"),
        JSCodable(
          ObjectInspector()
        )(tilesMode = false)
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

  val deck = builder.renderSlides


}
