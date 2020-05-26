package lavasite.lavadeck

import coderunner.{CodeRunner, IFrameCodeRunner, WorkerCodeRunner}
import com.wbillingsley.veautiful.{DiffNode, MutableArrayComponent}
import com.wbillingsley.veautiful.html.{<, SVG, VHtmlComponent, ^}
import com.wbillingsley.veautiful.templates.DeckBuilder
import lavamaze.{Maze, Snobot}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node, html, svg}
import lavasite.Common

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object FirstDeck {

  import scala.scalajs.js.JSConverters._



  val m = Maze()((10, 10), (10, 10))
  var text:String = ""

  val rpcs = Map[String, js.Function](
    "ping" -> (() => println("ping")),
    "goRight" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.EAST, i)).toJSPromise),
    "goLeft" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.WEST, i)).toJSPromise),
    "goUp" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.NORTH, i)).toJSPromise),
    "goDown" -> ((i: Int) => m.snobot.askF(Snobot.MoveMessage(lavamaze.SOUTH, i)).toJSPromise),
    "zing" -> { (i:Int, j:Int, k:Int) => println(s"Zinged with i=$i j=$j k=$j"); "Done" }
  )

  val cr = new WorkerCodeRunner(rpcs, Map.empty, true)

  val t = <.textarea(^.on("input") ==> { e =>
    text = e.target.asInstanceOf[html.TextArea].value
  })
  val b = <.button(^.onClick --> {
    //m.snobot.ask(Snobot.MoveMessage(lavamaze.EAST, 1), { _ => })
    println("code: " + text)
    CodeRunner.asyncBind(
      args = rpcs.toSeq,
      await=Seq("goRight", "goLeft", "goUp", "goDown"),
      code=text)()
  }, "Run locally")

  val test = <.button(^.onClick --> {
    cr.remoteExecute(text)
  }, "Run in worker")

  val icr = new IFrameCodeRunner(
    rpcs, Map.empty, true
  )(
    <.div(">.."), <.div("X")
  )

  val ifVersion = <.span(
    <.button(^.onClick --> icr.remoteExecute(text), "Run in iframe"), icr
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
        Seq(m, t, b, test, ifVersion, <.button(^.onClick --> {
          m.snobot.cancel()
          icr.reset()
          cr.reset()
        }, "Cancel"))
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
