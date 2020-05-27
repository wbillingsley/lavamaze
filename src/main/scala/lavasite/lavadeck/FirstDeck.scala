package lavasite.lavadeck

import coderunner.{CodeRunner, IFrameCodeRunner, WorkerCodeRunner}
import com.wbillingsley.veautiful.{DiffNode, MutableArrayComponent}
import com.wbillingsley.veautiful.html.{<, SVG, VHtmlComponent, ^}
import com.wbillingsley.veautiful.templates.{Challenge, DeckBuilder}
import lavamaze.{Maze, Snobot}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node, html, svg}
import lavasite.Common
import lavasite.templates.AceEditor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object FirstDeck {

  import scala.scalajs.js.JSConverters._

  val m = Maze()((10, 10), (10, 10))

  val rpcs = Map[String, js.Function](
    "ping" -> (() => println("ping")),
    "goRight" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.EAST, i)).toJSPromise),
    "goLeft" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.WEST, i)).toJSPromise),
    "goUp" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.NORTH, i)).toJSPromise),
    "goDown" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.SOUTH, i)).toJSPromise),
    "zing" -> { (i:Int, j:Int, k:Int) => println(s"Zinged with i=$i j=$j k=$j"); "Done" }
  )

  val cr = new WorkerCodeRunner(rpcs, Map.empty, true)

  val ace = AceEditor("mycode") { editor =>
    editor.setTheme("ace/theme/monokai")
    editor.setFontSize("24px")
    editor.session.setMode("ace/mode/javascript")
  }

  val b = <.button(^.onClick --> {
    for {
      e <- ace.editor
      text = e.getValue().asInstanceOf[String]
    } {
      CodeRunner.asyncBind(
        args = rpcs.toSeq,
        await = Seq("goRight", "goLeft", "goUp", "goDown"),
        code = text)()
    }
  }, "Run locally")

  val test = <.button(^.onClick --> {
    for {
      e <- ace.editor
      text = e.getValue().asInstanceOf[String]
    } cr.remoteExecute(text)
  }, "Run in worker")

  val icr = new IFrameCodeRunner(
    rpcs, Map.empty, true
  )(
    <.div(">.."), <.div("X")
  )

  val ifVersion = <.span(
    <.button(^.onClick --> {
      for {
        e <- ace.editor
        text = e.getValue().asInstanceOf[String]
      } icr.remoteExecute(text)
    }, "Run in iframe"), icr
  )




  val builder = new DeckBuilder()
    .markdownSlide(
      """
        |# The Lava Maze Test Deck
        |
        |A quick slide deck including some lava maze environments
        |
        |""".stripMargin).withClass("center middle")
    .veautifulSlide(
      <.div(
        <.h1("Maze"),
        Challenge.split(
          <.div(
            m,
          )
        )(
          <.div(^.attr("style") := "position: relative; width: 600px; height: 300px;",
            ace,
          ),
          Seq(b, test, ifVersion, <.button(^.onClick --> {
            m.snobot.cancel()
            icr.reset()
            cr.reset()
          }, "Cancel"))
        ),
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
