package lavasite.lavadeck

import com.wbillingsley.veautiful.html.*
import com.wbillingsley.veautiful.doctacular.Challenge
import Challenge.{Completion, Level}

import canvasland.{CanvasLand, Turtle}
import coderunner.{JSCodable, PrefabCodable, LoggingPrefabCodable}
import lavamaze.{FloorTile, Goal, Maze, Overlay}
import org.scalajs.dom.{Element, Node}

import lavasite.{markdown, styleSuite}
import scala.util.Random
import lavasite.lavadeck.StageCardsOfDoom.CardsOfDoom

object JellyFlood {
  styleSuite.addGlobalRules(
    """|@keyframes pulse-jelly {
       |    0% {
       |        opacity: 1;
       |        border-radius: 5px;
       |    }
       |
       |    70% {
       |        opacity: 0.6;
       |        border-radius: 20px;
       |    }
       |
       |    100% {
       |        opacity: 1;
       |        border-radius: 5px;
       |    }
       |}
       |""".stripMargin
  )
  styleSuite.update()

  val jellyGrid = Styling(
    """|
       |""".stripMargin
  ).modifiedBy(
    " .jelly-row" -> "display: grid; grid-template-columns: repeat(auto-fit, 60px);",
    " .jelly-cell" -> "width: 60px; height: 60px; border: 1px solid lightgray;",
    " .jelly-cell .floor" -> "background-color: #242424; width: 100%; height: 100%;",
    " .jelly-cell .lava" -> "background-color: darkred; width: 100%; height: 100%;",
    " .jelly-cell .jelly" -> "background-color: darkgreen; width: 100%; height: 100%; border-radius: 10px; color: white; text-align: center; line-height: 60px;",
    " .jelly-cell .jelly.active" -> "animation: pulse-jelly 2s infinite;"
  ).register()

}


case class JellyFlood(w:Int=8, h:Int=8, goalX:Int = 7, goalY:Int = 7, mazeString:Option[String] = None) extends DHtmlComponent {

  import scala.collection.mutable 
  val maze = mutable.Map.empty[(Int, Int), Boolean]
  val distance = mutable.Map.empty[(Int, Int), Int]
  var tick = 0

  def setSquare(x:Int, y:Int, c:Char):Unit = c match {
    case '.' => maze((x, y)) = true
    case '#' => maze((x, y)) = false
    case _ => // do nothing
  }

  def loadFromString(s:String) = {
    for {
      (line, y) <- s.linesIterator.zipWithIndex if y < h
      (char, x) <- line.zipWithIndex if x < w
    } {
      setSquare(x, y, char)
    }
  }


  private def check(p:(Int, Int), dist:Int):Unit = {
    distance(p) = dist
    val (x, y) = p

    for {
      (dx, dy) <- Seq((x+1, y), (x-1, y), (x, y+1), (x, y-1)) if (
        distance.getOrElse((dx, dy), Int.MaxValue) > dist + 1 &&
          maze.getOrElse(p, false)
        )
    } check((dx, dy), dist + 1)
  }

  def reset(): Unit = {
    tick = 0
    maze.clear()
    distance.clear()
    mazeString match {
      case Some(s) =>
        loadFromString(s)
      case _ =>
        for { x <- 0 to 1; y <- 0 to 1 } {
          maze((x,y)) = true
          maze((w - x - 1, h - y - 1)) = true
        }
        for { i <- 0 until 8 } {
          maze(4 -> i) = true
          maze(6 -> i) = true
          maze(i -> 4) = true
          maze(i -> 1) = true
        }
    }
    check((goalX, goalY), 0)
  }

  reset()

  override protected def render = <.div(
    <.div(^.cls := JellyFlood.jellyGrid,
      for {
        y <- 0 until h
      } yield <.div(^.cls := "jelly-row",
        for {
          x <- 0 until w
        } yield {
          val d =  distance.getOrElse(x -> y, Int.MaxValue)

          <.div(^.cls := "jelly-cell",
            if (maze.getOrElse(x -> y, false)) {
              if (tick > d) <.div(^.cls := "jelly", d.toString)
              else if (tick == d) <.div(^.cls := "jelly active", d.toString)
              else <.div(^.cls := "floor")
            } else <.div(^.cls := "lava")
          )
        }
      )
    ),
    <.div(^.cls := "btn-group",
      <.button(^.cls := "btn btn-outline-secondary", ^.onClick --> { tick = 0; rerender() }, "Reset"),
      <.button(^.cls := "btn btn-outline-primary", ^.onClick --> {
        tick = tick + 1
        rerender()
      }, "Step")
    )
  )
}


val exampleBlockStyling = Styling(
  """|display: inline-block;
     |padding: 2rem;
     |margin-right: 2rem;
     |border-radius: 2rem;
     |background: aliceblue;
     |vertical-align: top;
     |""".stripMargin
).register()

def exampleCode(s:String) = <.pre(^.cls := exampleBlockStyling, s)

val textColumnStyling = Styling("margin-top: 2rem; margin-left: 2rem; margin-right: 2rem;").register()
def textColumn(ac: DHtmlModifier *) = <.div(^.cls := s"text-column ${textColumnStyling.className}", <.div(ac:_*))


object SnobotChallenge {

  Challenge.split2Styling.addRules(".split2" -> "grid-gap: 2rem;")

  class SnobotStage(content:VHtmlContent, vs:(Int, Int) = 10 -> 10, ms:(Int, Int) = 10 -> 10,
             setup:Maze => _, beforeClass:Maze => _ = (_) => {}) extends Challenge.Stage {

    private val maze = Maze("exercise")(vs, ms)(setup)
    beforeClass(maze)

    override def completion: Completion = Challenge.Open
    override def kind = "exercise"

    private val codable = JSCodable(maze)(tilesMode = false)
    override protected def render = textColumn(
      <.div(^.cls := "lead", ^.attr("style") := "min-height: 200px; max-height: 300px; overflow-y: auto;", content), codable
    )
    
  }

  /** Adds X and Y functions for Snobot and Goal locations to a maze */
  def addXYFunctions(maze:Maze):Unit = {
    maze.additionalFunctions = Seq(
      ("getX", Seq.empty, () => maze.snobot.tx),
      ("getY", Seq.empty, () => maze.snobot.ty),
      ("getGoalX", Seq.empty, () => maze.getGoalX),
      ("getGoalY", Seq.empty, () => maze.getGoalY),
    )
  }

  def mdTask(md:String, vs:(Int, Int) = 10 -> 10, ms:(Int, Int) = 10 -> 10,
             setup:Maze => _, beforeClass:Maze => _ = (_) => {}) = new SnobotStage(markdown.div(md), vs, ms, setup, beforeClass)

  def vTask(content:VHtmlContent, vs:(Int, Int) = 10 -> 10, ms:(Int, Int) = 10 -> 10,
             setup:Maze => _, beforeClass:Maze => _ = (_) => {}) = new SnobotStage(content, vs, ms, setup, beforeClass)

  def smallMaze(s:String, vs:(Int, Int) = 10 -> 10, ms:(Int, Int) = 10 -> 10, setup:Maze => _, beforeClass:Maze => _ = (_) => {}) = {
    val m = Maze(s)(
      viewSize = vs,
      mazeSize = ms
    )(setup)

    beforeClass(m)
    m
  }

  val levels = Seq(
    Level(name = "Lava Maze - intro", stages = Seq(
      VNodeStage.twoColumn("Welcome to the Lava Maze")(() => markdown.div(
        s"""
           |Our first little game environment is about learning to code in JS. In each level, we're trying to get Snobot to the teleport.
           |
           |First, I'd better give you a quick rundown of the *functions* you can call in the environment:
           |
           |* `up()`, `down()`, `left()`, and `right()` make Snobot move one square.  \t
           |  Snobot can't walk through walls, and can't survive walking over lava, being caught by a blob guard, or
           |  being hit by a rolling boulder.
           |
           |* `canGoUp()`, `canGoDown()`, `canGoLeft()`, and `canGoRight()` return true if Snobot is stationary and
           |  Snobot thinks it can move safely into the square.
           |
           |Sometimes you'll have some code you're given on the right and a play button to demonstrate something.
           |Sometimes you'll have an area to write your own code.
           |
           |We're using JavaScript, so this first level's just a crib to help you with the syntax
           |
           |
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """while(canGoRight()) {
            |  right();
            |}
            |""".stripMargin,
          smallMaze("Hello world", 8 -> 3, 8 -> 3, setup = { maze =>
            maze.loadFromString(
              s"""
                | ZS...G
                |""".stripMargin)
          })
        ))
      ),
      vTask(<.div(
          <.h3("Right and down"),
          <.p("This maze is always the same size. Snobot just has to run 7 tiles right and 1 tile down. Try each of these."),
          <.p(
            exampleCode(
              """|let i = 0
                 |while (i < 7) {
                 |  right()
                 |  i = i + 1;
                 |}
                 |down()
                 |""".stripMargin
            ),
            exampleCode(
              """|for (let i = 0; i < 7; i++) {
                 |  right()
                 |}
                 |down()
                 |""".stripMargin
            ),
            exampleCode(
              """|while (canGoRight()) {
                 |  right()
                 |}
                 |down()             
                 |""".stripMargin
            )
          )
        ),
        setup = _.loadFromString(
          """
            |
            |
            | S.......
            |        G
            |
            |
            |
            |""".stripMargin)
      ),
      VNodeStage.twoColumn("Lone boulders can be pushed")(() => markdown.div(
        s"""
           |Snobot can push boulders, but he doesn't know he can until he tries.
           |
           |On the right, we have Snobot standing next to a boulder. When we ask him if he can go right, he'll say no.
           |But if we ask him to go right, he'll manage to push the boulder ahead of him.
           |""".stripMargin),
        () => VNodeStage.card(LoggingPrefabCodable(
          """if (canGoRight()) {
            |  println("Yes, I can go Right")
            |} else {
            |  println("No, there's a boulder in the way")
            |}
            |right()
            |down()
            |""".stripMargin,
          smallMaze("Hello world", 8 -> 4, 8 -> 4, setup = { maze =>
            maze.loadFromString(
              s"""
                 | SO...
                 |  G
                 |""".stripMargin)
          })
        ))
      ),
      mdTask(s"""### Blob Guards can be squashed
                |
                |Try making your way to the exit, pushing the boulder ahead of you. You should find you can squash
                |the Blob Guard with the boulder.
                |
                |""".stripMargin,
        setup = _.loadFromString(
          """
            |
            |
            | S.O....Z
            |        G
            |
            |
            |
            |""".stripMargin)
      ),
      mdTask(s"""### Diamonds are collectable and slippery
                |
                |We've popped in a boulder and a couple of diamonds and a boulder that wants to roll left.
                |
                |Try guiding snobot right 5 and down 1. You should find the blob guard doesnt have a good day.
                |""".stripMargin,
        setup = _.loadFromString(
          """
            |
            |
            | Z.S.*...
            |     *< G
            |     *
            |
            |
            |""".stripMargin)
      ),
    )),

    Level(name = "Algorithms", stages = Seq(
      VNodeStage.twoColumn("Algorithms for a changing world...")(() => markdown.div(
        s"""
           |An *algorithm* is a set of rules that can be used to solve a problem.
           |
           |So far, our algorithm to solve the maze has only worked for fixed mazes.
           |In these puzzles, though, we're going to set different mazes that will need different solutions.
           |
           |Sometimes, the maze is going to change randomly according to some set rules, **so you'll need to run the
           |program more than once to check your answer**.
           |
           |For example, the maze on the right reflects itself vertically every on every try, and the code will only
           |work for one orientation! Oops!
           |
           |Some challenges encourage you to code your solution in a particular way. For instance, defining your own
           |functions that you'll then call in order (so the higher-level algorithm is more obvious) or functions you
           |call from inside a loop.
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """while(canGoRight()) {
            |  right();
            |}
            |down();
            |""".stripMargin,
          {
            var parity = false
            smallMaze("Hello world", 8 -> 4, 8 -> 4, setup = { maze =>
              parity = !parity
              if (parity) {
                maze.loadFromString(
                  """
                    | S....
                    |     G
                    |""".stripMargin)
              } else {
                maze.loadFromString(
                  """
                    |     G
                    | S....
                    |""".stripMargin)
              }
            })
          }
        ))
      ),
      VNodeStage.twoColumn("The Cartesian Plane")(() => markdown.div(
        s"""
           |The Lava Maze is made up of tiles that have `x` and `y` coordinates.
           |
           |This is the same maze from before, but this time we've overlayed the `x`,`y` coordinates of each tile onto
           |the tiles. The tiles always know their coordinates, it's just that this time we've drawn them on the
           |screen.
           |
           |Now, if only we had a way of knowing if the goal was above or below us...
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """while(canGoRight()) {
            |  right();
            |}
            |down();
            |""".stripMargin,
          {
            var parity = false
            smallMaze("Hello world", 8 -> 4, 8 -> 4, setup = { maze =>
              parity = !parity
              maze.addOverlay(new Overlay.CoordinateOverlay(maze))

              if (parity) {
                maze.loadFromString(
                  """
                    | S....
                    |     G
                    |""".stripMargin)
              } else {
                maze.loadFromString(
                  """
                    |     G
                    | S....
                    |""".stripMargin)
              }
            })
          }
        ))
      ),
      {
        var parity = false
        mdTask(s"""### Topsy turvy world
                  |
                  |We've given you the maze from the previous page - that reflects every time you try it. It's your turn to solve it.
                  |
                  |We've taken away the overlay, but we've given you a couple of extra functions: `getY()` will return
                  |Snobot's current `y` coordinate, and `getGoalY()` will return the `y` coordinate of the goal.
                  |You might want to try `println(getY())` and `println(getGoalY())` first to see what they do.
                  |
                  |""".stripMargin,
          vs = 7 -> 4, ms = 7 -> 4,
          setup = (maze) => {
            parity = !parity

            if (parity) {
              maze.loadFromString(
                """
                  | S....
                  |     G
                  |""".stripMargin)
            } else {
              maze.loadFromString(
                """
                  |     G
                  | S....
                  |""".stripMargin)
            }
          },
          beforeClass = (maze) => maze.additionalFunctions = Seq(
            ("getY", Seq.empty, () => maze.snobot.ty),
            ("getGoalY", Seq.empty, () => maze.getGoalY),
          )
        )
      },
      VNodeStage.twoColumn("If the goal's above me, I want to go up...")(() => markdown.div(
        s"""
           |Snobot can push boulders, but he doesn't know he can until he tries.
           |
           |On the right, we have Snobot standing next to a boulder. When we ask him if he can go right, he'll say no.
           |But if we ask him to go right, he'll manage to push the boulder ahead of him.
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """while (canGoRight()) {
            |  right()
            |}
            |
            |if (getGoalY() > getY()) {
            |  down()
            |} else if (getGoalY() < getY()) {
            |  up()
            |}
            |""".stripMargin,
          {
            var parity = false
            smallMaze("Hello world", 8 -> 4, 8 -> 4, setup = { maze =>
              parity = !parity
              maze.addOverlay(new Overlay.CoordinateOverlay(maze))

              if (parity) {
                maze.loadFromString(
                  """
                    | S....
                    |     G
                    |""".stripMargin)
              } else {
                maze.loadFromString(
                  """
                    |     G
                    | S....
                    |""".stripMargin)
              }
            }, beforeClass = (maze) => maze.additionalFunctions = Seq(
              ("getY", Seq.empty, () => maze.snobot.ty),
              ("getGoalY", Seq.empty, () => maze.getGoalY),
            ))
          }
        ))
      ),
      mdTask(s"""### A little homing algorithm
                |
                |See if you can generalise the solution we had before to write a simple homing algorithm (the same one
                |the homing blob guard uses). This time, we've given you `getX()`, `getY()`, `getGoalX()`, and `getGoalY()`.
                |
                |The maze is always the same, but the goal and Snobot keep moving around...
                |""".stripMargin,
        setup = maze => {
          for { x <- 1 to 8; y <- 1 to 8 } maze.setTile(x, y, FloorTile)
          val sx = Random.nextInt(8) + 1
          val sy = Random.nextInt(8) + 1

          var (gx, gy) = (0, 0)
          while {
            gx = Random.nextInt(6) + 1
            gy = Random.nextInt(6) + 1

            gx == sx || gy == sy
          } do ()

          maze.snobotStart = (sx, sy)
          maze.addFixture(new Goal(gx, gy))
        },
        beforeClass = addXYFunctions _
      ),
      VNodeStage.twoColumn("Simple homing on a square")(() => markdown.div(
        s"""
           |You might find you end up with this kind of solution for our simple homing algorithm.
           |
           |But, of course, we've not taken account of any lava squares, falling boulders, or dead-ends in our way.
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """while (true) {
            |  if (getGoalY() > getY()) {
            |    down()
            |  } else if (getGoalY() < getY()) {
            |    up()
            |  } else if (getGoalX() < getX()) {
            |    left()
            |  } else if (getGoalX() > getX()) {
            |    right()
            |  }
            |}
            |""".stripMargin,
            smallMaze("Hello world", vs = 6 -> 6, ms = 6 -> 6,
              setup = maze => {
                for { x <- 1 to 4; y <- 1 to 4 } maze.setTile(x, y, FloorTile)
                val sx = Random.nextInt(4) + 1
                val sy = Random.nextInt(4) + 1

                var (gx, gy) = (0, 0)
                while {
                  gx = Random.nextInt(4) + 1
                  gy = Random.nextInt(4) + 1

                  gx == sx || gy == sy
                } do ()

                maze.snobotStart = (sx, sy)
                maze.addFixture(new Goal(gx, gy))
              },
              beforeClass = maze => {
                maze.additionalFunctions = Seq(
                  ("getX", Seq.empty, () => maze.snobot.tx),
                  ("getY", Seq.empty, () => maze.snobot.ty),
                  ("getGoalX", Seq.empty, () => maze.getGoalX),
                  ("getGoalY", Seq.empty, () => maze.getGoalY),
                )
              }
            )
        ))
      ),

      StageCardsOfDoom

    )),

    Level(name = "Flood-fill pathfinding", stages = Seq(
      VNodeStage.twoColumn("We've reached a dead end...")(() => markdown.div(
        s"""
           |Unfortunately, our simple homing algorithm doesn't cope very well with dead ends. It happily plunges down
           |blind alleys without any hope of getting out again.
           |
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """while (true) {
            |  if (getGoalY() > getY() && canGoDown()) {
            |    down()
            |  } else if (getGoalY() < getY() && canGoUp()) {
            |    up()
            |  } else if (getGoalX() < getX() && canGoLeft()) {
            |    left()
            |  } else if (getGoalX() > getX() && canGoRight()) {
            |    right()
            |  }
            |}
            |""".stripMargin,

            smallMaze("Hello world", 8 -> 6, 8 -> 6, setup = { maze =>
                maze.loadFromString(
                  """
                    | S.....
                    |    .
                    |    ...
                    |    . G
                    |""".stripMargin)
            }, beforeClass = addXYFunctions _)
        ))
      ),

      VNodeStage.twoColumn("Jelly flood!")(() => markdown.div(
        s"""
           |Imagine someone is pouring jelly onto the goal square.
           |
           |After zero turns, it's only on the goal square.  \t
           |After one turn, it'd move to the neighbouring squares.  \t
           |After two turns, it'd move to their neighbours.  \t
           |
           |If we count how long it takes to reach a square, that's the *distance from the goal*.
           |
           |We can then use this in an algorithm - we move to any square that has a lower goal distance than our own.
           |""".stripMargin),
        () => VNodeStage.card(
          JellyFlood()
        )
      ),
      {
        val overlay = new Overlay.FloodFill()

        mdTask(
          """## Countdown...
            |
            |In this exercise, we've given you `getX()`, `getY()`, and `getGoalDistance(x, y)`. See if you can find
            |your way to the exit...
            |""".stripMargin,
          setup = maze => {
            maze.addOverlay(overlay)
            for { x <- 0 to 1; y <- 0 to 1 } {
              maze.setTile(x,y, FloorTile)
              maze.setTile(10 - x - 1, 10 - y - 1, FloorTile)
            }

            for {
              mid <- Seq(Random.nextInt(7) + 2, Random.nextInt(7) + 2)
            } {
              for { i <- 1 until 10 } maze.setTile(i, 1, FloorTile)
              for { i <- 0 until 10 } maze.setTile(mid, i, FloorTile)
              for { i <- mid until 9 } maze.setTile(i, 8, FloorTile)
            }
            maze.addFixture(new Goal(9, 9))
          },
          beforeClass = maze => {
            maze.additionalFunctions = Seq(
              ("getX", Seq.empty, () => maze.snobot.tx),
              ("getY", Seq.empty, () => maze.snobot.ty),
              ("getGoalDistance", Seq("number", "number"), (x:Int, y:Int) => {
                overlay.distanceMap.getOrElse(x -> y, Int.MaxValue)
              })
            )
          }
        )
      },

      VNodeStage.twoColumn("Exiting by numbers")(() => markdown.div(
        s"""
           |The code on the right should solve most mazes.
           |
           |This is where we're going to leave our automatic pathfinding for now - we'll come back to it when we
           |need to *explore* mazes to solve them much later.
           |
           |In our games, this kind of totally automatic algorithm would still be thwarted by obstacles. As we'll see on
           |the next slide, just a boulder in the way would stop our jelly from reaching us.
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """while (getGoalDistance(getX(), getY()) > 0) {
            |  let x = getX()
            |  let y = getY()
            |  let g = getGoalDistance(x, y)
            |
            |  if (getGoalDistance(x+1, y) < g) {
            |    right()
            |  } else if (getGoalDistance(x-1, y) < g) {
            |    left()
            |  } else if (getGoalDistance(x, y+1) < g) {
            |    down()
            |  } else if (getGoalDistance(x, y-1) < g) {
            |    up()
            |  }
            |}
            |""".stripMargin,

          {
            val overlay = new Overlay.FloodFill()
            smallMaze("Hello world", 8 -> 6, 8 -> 6, setup = { maze =>
              maze.addOverlay(overlay)
              for { x <- 0 to 1; y <- 0 to 1 } {
                maze.setTile(x,y, FloorTile)
                maze.setTile(8 - x - 1, 6 - y - 1, FloorTile)
              }

              for {
                mid <- Seq(Random.nextInt(7) + 2, Random.nextInt(7) + 2)
              } {
                for { i <- 1 until 8 } maze.setTile(i, 1, FloorTile)
                for { i <- 0 until 6 } maze.setTile(mid, i, FloorTile)
                for { i <- mid until 7 } maze.setTile(i, 4, FloorTile)
              }
              maze.addFixture(new Goal(7, 5))
            }, beforeClass = maze => {
              maze.additionalFunctions = Seq(
                ("getX", Seq.empty, () => maze.snobot.tx),
                ("getY", Seq.empty, () => maze.snobot.ty),
                ("getGoalDistance", Seq("number", "number"), (x:Int, y:Int) => {
                  overlay.distanceMap.getOrElse(x -> y, Int.MaxValue)
                })
              )
            })
          }
        ))
      ),

    )),

    Level(name = "A couple of puzzles", stages = Seq(

      {
        var parity = false
        mdTask(
          """## Give 'em the runaround!
            |
            |There is a homing blob guard between you and the exit. Lead it a merry chase.
            |
            |If you need Snobot to pause, you might find `uptime()` useful. This function will give you the number of
            |milliseconds Snobot has been online for. You can read its value once into a constant and then write a loop
            |that calls uptime() until it's been long enough.
            |""".stripMargin,
          setup = maze => {
            parity = !parity
            maze.loadFromString(
                """
                  |##########
                  |##.*...###
                  |##.###.###
                  |S..###..ZG
                  |##.###.###
                  |##.1...###
                  |##########
                  |""".stripMargin
            )
          },
        )
      },

    {
      var parity = false
      vTask(
        <.div(
          <.h2("Don't get trapped!"),
          <.p("You'll need to collect all the diamonds to get through the gate. Hint: to define your own function, so you can call it repeatedly:"),
          <.p(
            exampleCode("""|function myFunction() {
                           |  right()
                           |  down()
                           |  // etc
                           |}
                           |""".stripMargin)
          ),          
        ),
        setup = maze => {
          parity = !parity
          maze.loadFromString(
              """
                |
                |###########
                |S..vvvv.5G#
                |..*****####
                |########
                |""".stripMargin
          )
        },
      )
    },

    ))
  )

}