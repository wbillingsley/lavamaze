package lavasite.trythisathome

import com.wbillingsley.veautiful.doctacular.Challenge
import lavasite.{*, given}
import lavadeck.*
import coderunner.{JSCodable, PrefabCodable, LoggingPrefabCodable}

import scala.util.Random



import com.wbillingsley.veautiful.html.*
import lavamaze.Maze
import lavamaze.FloorTile
import lavamaze.Goal
import lavamaze.Overlay
import canvasland.{Turtle, CanvasLand}
import canvasland.LineTurtle
import canvasland.RescueLine
import lavasite.lavadeck.LineBotDeck.applyStandardInaccuracy


type HappyGrid = ((Int, Int), (Int, Int))

extension (h:HappyGrid) {
    def happy:Boolean = 
        val ((a, b), (_, _)) = h
        b % 2 == 0
}

val happyStyle = new Styling(
    """|
       |border: 1px solid gray;
       |font-size: 25px;
       |display: inline-block;
       |margin: 1em;
       |font-family: "Fira Code";
       |
       |""".stripMargin
).modifiedBy(
    " td" -> "width: 75px; height: 50px; text-align: center; padding: 5px;",
    " .numbers" -> "font-size: 25px; font-weight: bold;",
    " .happy" -> "color: green;",
    " .unhappy" -> "color: red;",
)
.register()

val scilly = <.img(^.src := "https://theintelligentbook.com/thinkingaboutprogramming/images/control/scilly%20isles.jpg").build().create()

case class HappyGame() extends DHtmlComponent {

    val maxNum = 10

    def newGrid = ((Random.nextInt(maxNum), Random.nextInt(maxNum)), (Random.nextInt(maxNum), Random.nextInt(maxNum)))
    
    val state = stateVariable(false)
    val grids = stateVariable(List(newGrid))

    def pushGrid():Unit = 
        grids.value = newGrid :: grids.value
        state.value = false

    def showHappiness():Unit =
        state.value = true

    def happyClass(show:Boolean, state:Boolean):String = 
        if show then 
            if state then "happy" else "unhappy"
        else "unknown"

    def renderGrid(h:HappyGrid, showHappy:Boolean = false) = {
        val ((a, b), (c, d)) = h

        <.div(^.cls := happyStyle,
            <.table(^.cls := happyClass(showHappy, h.happy),
                <.tr(^.cls := "numbers",
                    <.td(a.toString), <.td(b.toString)
                ),
                <.tr(^.cls := "numbers",
                    <.td(c.toString), <.td(d.toString)
                ),
                (if showHappy then 
                    <.tr(
                        <.td(^.cls := happyClass(showHappy, h.happy), ^.attr.colspan := "2", if h.happy then "Happy 😊" else "Sad 😞")
                    )
                else
                    <.tr(
                        <.td(^.cls := happyClass(showHappy, h.happy), ^.attr.colspan := "2", "?")
                    )
                )
            )
        ) 
    }

    override def render = {
        val head :: tail = grids.value

        <.div(^.style := "overflow-y: auto; max-height: 900px;",
            renderGrid(head, state.value),

            (if state.value then 
                <.p(
                    <.button(^.cls := "btn btn-primary", "New grid", ^.onClick --> pushGrid())
                )
            else 
                <.p(
                    <.button(^.cls := "btn btn-primary", "Show happiness", ^.onClick --> showHappiness())
                )
            ),
        

            <.div(
                <.h3("Previously..."),
                for g <- tail yield renderGrid(g, true)
            )        
        )
       
    }


}



val intro = Challenge.Level("Do Try This at Home - Intro", Seq(
    VNodeStage.hero(<.div(^.style := "padding: 5em;",
        <.h1(^.style := "font-family: 'Bungee Spice'; font-size: 96px;", "Do Try This at Home*"),
        <.p(^.style := "font-size: 60px;", "A coding and computing workshop"),
        <.p(^.style := "margin-top: 15em;", "* Ok, these are slides for an in-person workshop, but you can do most of the games we'll show at home too!")
    )),
    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Machine learning"),
        markdown.div(
            """|*Which of my cue phrases did that sound like?*
               |
               |e.g. 
               |
               |> "That was a bit long-winded"
               |>
               |> "Wasn't that long"
               |>
               |> "You could have said that shorter"
               |
               |all produce
               |
               |`LongWindedIntent`
               |
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
      right = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Programming"),
        markdown.div(
            """|If you receive 
               |
               |`LongWindedIntent`
               |
               |then say 
               |
               |> "I'm an AI that learns from you. It's not my fault if you're a windbag"
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
    ),

    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Human learning"),
        markdown.div(
            """|The grids on the right have a rule determining if they're `Happy` or `Unhappy`.
               |
               |Your challenge is to figure out the rule!
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
      right = () => VNodeStage.card(
        HappyGame()
      ) 
    ),

    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Machine Learning"),
        markdown.div(
            """|Here's one Google made earlier, that runs in a web-browser...
               |
               |[Teachable Machine, version 1](https://teachablemachine.withgoogle.com/v1/)
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
      right = () => <.div()
    ),

    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Let's play a game"),
        markdown.div(
            """|Now we can control Pac-Man ... badly!
               |
               |[Tensorflow.js Pac-Man demo](https://storage.googleapis.com/tfjs-examples/webcam-transfer-learning/dist/index.html)
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
      right = () => <.div()
    ),

))


val snobot = Challenge.Level("Snobot and the Lava Maze", Seq(
    VNodeStage.twoColumn("")(
        () => <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Escape the Lava Maze"),
        () => VNodeStage.card(PrefabCodable(
          """while(canGoRight()) {
            |  right();
            |}
            |""".stripMargin,
          SnobotChallenge.smallMaze("Hello world", 8 -> 3, 8 -> 3, setup = { maze =>
            maze.loadFromString(
              s"""
                | ZS...G
                |""".stripMargin)
          })
        ))
    ),

    SnobotChallenge.vTask(<.div(
          <.h3("Right and down"),
        //   <.p("This maze is always the same size. Snobot just has to run 7 tiles right and 1 tile down. Try each of these."),
        //   <.p(
        //     exampleCode(
        //       """|let i = 0
        //          |while (i < 7) {
        //          |  right()
        //          |  i = i + 1;
        //          |}
        //          |down()
        //          |""".stripMargin
        //     ),
        //     exampleCode(
        //       """|for (let i = 0; i < 7; i++) {
        //          |  right()
        //          |}
        //          |down()
        //          |""".stripMargin
        //     ),
        //     exampleCode(
        //       """|while (canGoRight()) {
        //          |  right()
        //          |}
        //          |down()             
        //          |""".stripMargin
        //     )
        //   )
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

      SnobotChallenge.mdTask(s"""### A little homing algorithm
                |
                |See if you can generalise the solution we had before to write a simple homing algorithm (the same one
                |the homing blob guard uses). This time, we've given you `getX()`, `getY()`, `getGoalX()`, and `getGoalY()`.
                |
                |The maze is always the same, but the goal and Snobot keep moving around...
                |""".stripMargin,
        setup = (maze:Maze) => {
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
        beforeClass = SnobotChallenge.addXYFunctions _
      ),      

      SnobotChallenge.mdTask(s"""### A little homing algorithm
                |
                |See if you can generalise the solution we had before to write a simple homing algorithm (the same one
                |the homing blob guard uses). This time, we've given you `getX()`, `getY()`, `getGoalX()`, and `getGoalY()`.
                |
                |The maze is always the same, but the goal and Snobot keep moving around...
                |""".stripMargin,
        setup = (maze:Maze) => {
          maze.addOverlay(new Overlay.CoordinateOverlay(maze))

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
        beforeClass = SnobotChallenge.addXYFunctions _
      ),

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

            SnobotChallenge.smallMaze("Hello world", 8 -> 6, 8 -> 6, setup = { maze =>
                maze.loadFromString(
                  """
                    | S.....
                    |    .
                    |    ...
                    |    . G
                    |""".stripMargin)
            }, beforeClass = SnobotChallenge.addXYFunctions _
        ))
      )),

))


val algs = Challenge.Level("Algorithms", Seq(
    StageCardsOfDoom,

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

    VNodeStage.twoColumn("Exiting by numbers")(() => markdown.div(
        s"""
           |The code on the right should solve most mazes.
           |           
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
            SnobotChallenge.smallMaze("Hello world", 8 -> 6, 8 -> 6, setup = { (maze:Maze) =>
              maze.addOverlay(overlay)

              maze.loadFromString(
                  """
                    | S.....
                    |    .
                    |    ...
                    |    . G
                    |""".stripMargin)


            }, beforeClass = (maze:Maze) => {
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

    ))


val sensors = Challenge.Level("Sensors and Feedback", Seq(

   VNodeStage.twoColumn("")(
      left = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Micro:Bit"),
        markdown.div(
            """|One of the first microbit projects is a soil moisture sensor
               |
               |[Project on Makecode](https://makecode.microbit.org/projects/soil-moisture)
               |
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
      right = () => <.div(
        <.img(^.src := "https://pxt.azureedge.net/blob/3fbfadd2007554af832353d44822d23520f5ea98/static/mb/projects/soil-moisture/soil-moisture.jpg")
      )(^.style := "font-size: 200%;"), 
    ),

    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Feedback"),
        markdown.div(
            """|We need a brave volunteer to walk six steps ... blindfolded
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
      right = () => <.div()
    ),

    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.img(^.src := "https://theintelligentbook.com/thinkingaboutprogramming/images/control/dead%20reckoning.jpg")
      )(^.style := "font-size: 200%;"), 
      right = () => <.div()
    ),

    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.img(^.src := "https://theintelligentbook.com/thinkingaboutprogramming/images/control/dead%20reckoning%20error.jpg")
      )(^.style := "font-size: 200%;"), 
      right = () => <.div()
    ),

    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.img(^.src := "https://theintelligentbook.com/thinkingaboutprogramming/images/control/scilly%20isles.jpg")
      )(^.style := "font-size: 200%;"), 
      right = () => <.div()
    ),


    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.img(^.src := "https://upload.wikimedia.org/wikipedia/commons/d/d5/HMS_Association_%281697%29.jpg")
      )(^.style := "font-size: 200%;"), 
      right = () => <.div()
    ),


    VNodeStage.twoColumn("")(
      left = () => <.div(
        <.div(
            <.h2("Sailing into the Channel"),
            Common.markdown(
                """We should be able to sail into the Channel with
                |
                |```js
                |left(30); forward(600); right(20); forward(300);
                |```
                |
                |But what if storms, winds, or currents means our first leg was a little bit wrong?
                |""".stripMargin),
            JSCodable(CanvasLand()(
                viewSize = 920 -> 640,
                fieldSize = 1280 -> 720,
                r = Turtle(50, 600),
                setup = (c) => {
                    c.drawImage(scilly, 0, 0,1280, 720, 0,0,1280, 720)
                }
            ))(codeCanvasWidth = 480, buttonDrawerWidth = 0, tilesMode = false)
        )
      ), 
      right = () => <.div()
    ),


    VNodeStage.twoColumn("")(
      left = () => <.div(
        Common.markdown(
        """|Line-following with a single sensor...
           |
           |```
           |addLineSensor(20, 0, 255, 0, 0);
           |
           |while (true) {
           |    if (readSensor(0) > 0.5) {
           |        right(2);
           |        forward(1);
           |    } else {
           |        left(2);
           |        forward(1);
           |    }
           |}
           |```
           |""".stripMargin),
          JSCodable(CanvasLand()(
            fieldSize=(920 -> 640),
            viewSize=(860 -> 640),
            r = LineTurtle(120, 100) { r =>  applyStandardInaccuracy(r) },
            setup = c => {
            c.fillCanvas("white")
            c.drawGrid("rgb(200,240,240)", 25, 1)
            c.withCanvasContext { ctx =>
                ctx.strokeStyle = "rgb(60,60,60)"
                ctx.lineWidth = 25
                ctx.beginPath()
                ctx.moveTo(100, 100)
                ctx.lineTo(770, 100)
                ctx.lineTo(770, 540)
                ctx.bezierCurveTo(670, 540, 150, 200, 150, 100)
                ctx.stroke()
            }
            }
        ))(tilesMode = false, buttonDrawerWidth = 0, codeCanvasWidth = 600)
      ), 
      right = () => <.div()
    ),

VNodeStage.twoColumn("Rescue Line")(() => Common.markdown(
        s"""
           |This is probably a bit big to code live, but if we can have more sensors, we can do things like "turn left on green"
           |""".stripMargin),
        () => VNodeStage.card(PrefabCodable(
          """addLineSensor(20, -6, 0, 255, 0) // line sensor, green only
            |addLineSensor(20, 6, 0, 255, 0) // line sensor, green only
            |addLineSensor(8, 8, 255, 0, 255) // junction sensor, a little closer and wider
            |
            |addLineSensor(0, 0, 255, 0, 0) // stop sensor - red component
            |addLineSensor(0, 0, 0, 255, 255) // stop sensor - non-red component
            |
            |let stop = false
            |
            |while (!stop) {
            |  let l = readSensor(0) > 0.5
            |  let r = readSensor(1) > 0.5
            |
            |  let jr = readSensor(2) < 0.5 // if this goes dark, turn right
            |
            |  stop = readSensor(3) > 0.5 && readSensor(4) < 0.4
            |  console.log(stop)
            |
            |  if (l && r) {
            |    forward(2)
            |  } else if (l && !r) {
            |    right(3)
            |    forward(1)
            |  } else if (!l && r) {
            |    left(2)
            |    forward(1)
            |  } else {
            |    if (jr) {
            |      forward(1)
            |      right(5)
            |    } else {
            |      forward(1)
            |    }
            |  }
            |}
            |""".stripMargin,
          LineBotDeck.smallMaze("Hello world", inaccurate = false) { ctx =>
            RescueLine.start(0, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.straight(1, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.straight(2, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.straight(3, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.teeRight(4, 0, RescueLine.FACING_EAST, ctx)
            RescueLine.roundaboutLeft(4, 1, RescueLine.FACING_EAST, ctx)
            RescueLine.straight(3, 1, RescueLine.FACING_EAST, ctx)
            RescueLine.dashed(2, 1, RescueLine.FACING_EAST, ctx)
            RescueLine.crossRoad(1, 1, RescueLine.FACING_EAST, ctx)
            RescueLine.end(0, 1, Math.PI, ctx)
          }, codeStyle = Some("max-height: 400px;")
        ))
      ),



))

val moon = 
    import LanderChallenge.* 
    import canvasland.LunarLanderSim

    Challenge.Level("Landing on the Moon", Seq(

        TextBelowLanderStage(<.div(
                <.pre(^.style := "display: inline-block; overflow: auto; max-height: 300px;", 
                """|
                    |// Our thrust is proportional to how much we need to slow down
                    |function control(vy) {
                    |  const target = 2
                    |  let err = getVy() - target
                    |  let thrust = 0.9 * err
                    |  return Math.max(0, Math.min(1, thrust))
                    |}
                    |
                    |// Let it get too fast
                    |wait(250)
                    |
                    |// Now control the descent
                    |while (true) {
                    |  let t = control(getVy())
                    |  setThrust(t)
                    |  println(t)
                    |}
                    |""".stripMargin
                
                ),
                <.pre(^.style := "display: inline-block; overflow: auto; max-height: 300px;", 
                """|function tooFast() {
                    |  return getVy() > 2
                    |}
                    |
                    |// Let it get too fast
                    |wait(250)
                    |
                    |while (true) {
                    |  if (tooFast()) {
                    |    setThrust(1)
                    |  } else {
                    |    setThrust(0)
                    |  }
                    |}
                    |""".stripMargin
                
                )
            ),
            LunarLanderSim("lander")(onReset = { sim =>
            sim.world.gravity.y = 0.16

            sim.Lander.setPosition(4900, 500)
            })
        ),

        TextBelowLanderStage(<.div(
                <.pre(^.style := "display: inline-block; overflow: auto; max-height: 300px;", 
                """|
                    |function getAngleErr() {
                    |    return getAngle() + 30 * getAngularVelocity()
                    |}
                    |
                    |function tilted() {
                    |    return Math.abs(getAngle()) > 0.25
                    |}
                    |
                    |function rightTheShip() {
                    |    let e = angleErr()
                    |    if (Math.cos(e) < 0.995) {
                    |        if (Math.sin(e) > 0) {
                    |            setTurnThrust(-1)
                    |        } else {
                    |            setTurnThrust(1)
                    |        }
                    |    } else setTurnThrust(0)
                    |}
                    |
                    |while (true) {
                    |    rightTheShip()
                    |}
                    |""".stripMargin
                
                ),
                <.pre(^.style := "display: inline-block; overflow: auto; max-height: 300px;", 
                """|function getSideErr() {
                   |    return 4900 - (getX() + 75 * getVx())
                   |}
                   |
                   |function lineUp() {
                   |    let e = getSideErr()
                   |    if (Math.abs(e) > 10) {
                   |        if (e > 0) {
                   |            setSideThrust(-1)
                   |        } else {
                   |            setSideThrust(1)
                   |        }
                   |    } else setSideThrust(0)
                   |}
                   |
                   |function linedUp() {
                   |    return Math.abs(4900 - getX()) < 10
                   |}
                   |
                   |while (true) {
                   |    rightTheShip()
                   |    if (!tilted()) {
                   |        lineUp()
                   |    }
                   |}
                    |""".stripMargin
                
                ),
                <.pre(^.style := "display: inline-block; overflow: auto; max-height: 300px;", 
                """|function getAngleErr() {
                   |    return getAngle() + 30 * getAngularVelocity()
                   |}
                   |
                   |function rightTheShip() {
                   |    let e = getAngleErr()
                   |    if (Math.cos(e) < 0.995) {
                   |        if (e > 0) {
                   |            setTurnThrust(-1)
                   |        } else {
                   |            setTurnThrust(1)
                   |        }
                   |    } else setTurnThrust(0)
                   |}
                   |
                   |function tilted() {
                   |    return Math.abs(getAngle()) > 0.25
                   |}
                   |
                   |
                   |function getSideErr() {
                   |    return 4920 - (getX() + 90 * getVx())
                   |}
                   |
                   |function lineUp() {
                   |    let e = getSideErr()
                   |    if (Math.abs(e) > 5) {
                   |        if (e > 0) {
                   |            setSideThrust(0.7)
                   |        } else {
                   |            setSideThrust(-0.7)
                   |        }
                   |    } else setSideThrust(0)
                   |}
                   |
                   |function linedUp() {
                   |    return Math.abs(4920 - getX()) < 10
                   |}
                   |
                   |function getVertErr(target) {
                   |    return target - (getY() + 90 * getVy())
                   |}
                   |
                   |function hover(target) {
                   |    let e = getVertErr(target)
                   |    if (Math.abs(e) > 20) {
                   |        if (e > 0) {
                   |            setThrust(0)
                   |        } else {
                   |            setThrust(1)
                   |        }
                   |    } else setThrust(0)
                   |}
                   |
                   |function landed() {
                   |    return Math.abs(4100 - getY()) < 20
                   |}
                   |
                   |while (!landed()) {
                   |    rightTheShip()
                   |    if (!tilted()) {
                   |        lineUp()
                   |        if (linedUp()) {
                   |            hover(4100)
                   |        } else {
                   |            hover(500)
                   |        }
                   |    }
                   |}
                   |setSideThrust(0)
                   |setThrust(0)
                   |setTurnThrust(0)
                   |
                   |""".stripMargin                
                )
            ),
            LunarLanderSim("lander")(onReset = { sim =>
            sim.world.gravity.y = 0.16

            sim.Lander.setPosition(1000, 500)
            sim.Lander.angle = Random.nextDouble() * 2 * Math.PI
            })
        ),



    ))


val sonic = Challenge.Level("Sonic Pi", Seq(

   VNodeStage.twoColumn("")(
      left = () => <.div(
        <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Sonic Pi"),
        markdown.div(
            """|Programmable music
               |
               |[Sonic Pi](https://sonic-pi.net/)
               |
               |""".stripMargin
        )
      )(^.style := "font-size: 200%;"), 
      right = () => <.div() 
    ),
))

val levels = Seq(intro, snobot, algs, sensors, moon, sonic)
