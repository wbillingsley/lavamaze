package lavasite.lavadeck

import canvasland.{MicroRat, CanvasLand, LineTurtle, LunarLanderSim, RescueLine, Turtle, Vec2}
import coderunner.JSCodable
import com.wbillingsley.veautiful.html.<
import com.wbillingsley.veautiful.templates.DeckBuilder
import lavamaze.{Gate, Maze, Overlay}
import lavasite.Common
import structures.ObjectInspector

import scala.util.Random
import coderunner.StructureVis

object LineBotDeck {

  val landerSim = new LunarLanderSim("lander")(onReset = { sim =>
    sim.world.gravity.y = 0.16

    sim.Lander.setPosition(1000, 500)
    sim.Lander.angle = Random.nextDouble() * 2 * Math.PI
  })

  val bilbySim = new MicroRat()({ sim =>
    sim.loadMazeFromString(
      """..........
        |.###.###..
        |..#...#..#
        |#.#.##.#..
        |....#...#.
        |##.##.#.#.
        |....###.#.
        |.####.....
        |......####
        |.###.....#
        |""".stripMargin)

  }, { sim => })

  val builder = new DeckBuilder()(using lavasite.markdown)
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
        r = LineTurtle(150, 100) { r => },
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
    .veautifulSlide(<.div(
      <.h1("Lunar Lander"),
      JSCodable(landerSim.canvasLand)(tilesMode = false)
    ))
    .veautifulSlide(<.div(
      <.h1("Moving Bumper"),
      JSCodable(CanvasLand()(
        fieldSize=(1000 -> 1000),
        r = bilbySim.robot,
        setup = c => {
          c.fillCanvas("black")
          bilbySim.paintCanvas(c)
          c.addSteppable(bilbySim.Goal)
          c.addSteppable(bilbySim)
        }
      ))(codeCanvasWidth= 800,tilesMode = false)
    ))
    .veautifulSlide(<.div(
      <.h1("LineBot"),
      JSCodable(CanvasLand()(
        fieldSize=(1000 -> 1000),
        r = LineTurtle(RescueLine.halfTile, RescueLine.halfTile) { r =>
          r.penDown = false
          r.moveWobble = Random.nextDouble * 0.02
          r.moveWobbleBias = (Random.nextDouble - 0.5) * 0.0001
          r.turnInaccuracy = Random.nextDouble * 0.1
          r.turnBias = (Random.nextDouble - 0.5) * 0.1
          r.moveInaccuracy = Random.nextDouble * 0.1
          r.moveBias = (Random.nextDouble - 0.5) * 0.1
        },
        setup = c => {
          c.fillCanvas("white")
          c.drawGrid("rgb(200,240,240)", RescueLine.tileSize, 1)
          c.withCanvasContext { ctx =>
            RescueLine.start(0, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.dashed(1, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.straight(2, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.sharpTurnRight(3, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.dashedHump(3, 1, RescueLine.FACING_SOUTH, ctx)
            RescueLine.end(3, 2, RescueLine.FACING_SOUTH, ctx)
            RescueLine.rescueZone(3, 3, RescueLine.FACING_EAST, ctx)
            RescueLine.rescueSurvivor(3, 4, RescueLine.FACING_EAST, ctx)
            RescueLine.roundaboutAhead(1, 4, RescueLine.FACING_EAST, ctx)
          }
        }
      ))(tilesMode = false)
    ))
    .markdownSlide(Common.willCcBy).withClass("bottom")

  val deck = builder.renderSlides


}
