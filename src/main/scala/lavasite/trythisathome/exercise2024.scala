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

// For circuits
import circuitsup.Common
import circuitsup.booleanlogic.BooleanTopic.{nextButton, onCompletionUpdate}
import circuitsup.templates.ExerciseStage
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.<
import com.wbillingsley.veautiful.doctacular.Challenge
import com.wbillingsley.veautiful.doctacular.Challenge.{Complete, Open}
import com.wbillingsley.wren.Orientation.East
import com.wbillingsley.wren.Wire._
import com.wbillingsley.wren._
import org.scalajs.dom.{Element, Node}


val levels2024 = Seq( 

    Challenge.Level("How do machines learn?", Seq(
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
            <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Machine Learning"),
            markdown.div(
                """|Let's teach it to recognise drawings
                |
                |[Teachable Machine, image task](https://teachablemachine.withgoogle.com/train/image)
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),

        VNodeStage.twoColumn("")(
        left = () => <.div(
            <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Quick, Draw"),
            markdown.div(
                """|And now for a bit of fun...
                |
                |[Quick Draw](https://quickdraw.withgoogle.com/)
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),        


    )),

    Challenge.Level("How do machines remember?", Seq(

        VNodeStage.twoColumn("")(
        left = () => <.div(
            markdown.div(
                """|Let's make a NAND gate out of you...
                |
                |[NAND gates](https://theintelligentbook.com/circuitsup/#/boolean/2/0)
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),        

        VNodeStage.twoColumn("")(
        left = () => <.div(
            markdown.div(
                """|Time to get our wires crossed
                |
                |[Latch](https://theintelligentbook.com/circuitsup/#/latches/1/0)
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),  

        VNodeStage.twoColumn("")(
        left = () => <.div(
            markdown.div(
                """|But there's a problem!
                |
                |(signal race demo)
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),  

        VNodeStage.twoColumn("")(
        left = () => <.div(
            markdown.div(
                """|So really, it ends up more complicated
                |
                |[Flip-flop](https://theintelligentbook.com/circuitsup/#/latches/1/3)
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),  

    )),

    Challenge.Level("How do machines create CHAOS?", Seq(

        VNodeStage.twoColumn("")(
        left = () => <.div(
            markdown.div(
                """|Conway's Game of Life - two simple rules
                   |
                   |* If a live cell has exactly 2 or 3 live neighbours, it stays alive. Otherwise, it dies.
                   |* If a dead cell has exactly 3 live neighbours, it comes alive. Otherwise it stays dead.
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),        

        VNodeStage.twoColumn("Complexity from simplicity")(() => markdown.div(
        s"""
           |Before we start coding, I'd like to give you the idea that coding can use simple rules to create very complex stuff.
           |So, we're going to play around for a few minutes with about the simplest game (well, toy really) you could imagine -
           |
           |John Conway's "Game of Life"
           |
           |On the right, there's a grid of "cells". Click a cell to toggle it "alive" or "dead"
           |
           |The game only has two rules. When you hit the play button, on every tick
           |
           |* If a live cell has exactly 2 or 3 live neighbours, it stays alive. Otherwise, it dies.
           |* If a dead cell has exactly 3 live neighbours, it comes alive. Otherwise it stays dead.
           |
           |
           |Controls
           |* Click a cell to toggle it
           |* <i class="material-symbols-outlined">play_arrow</i> to start playing at a tick every quarter-second
           |* <i class="material-symbols-outlined">stop</i> to stop ticking
           |* <i class="material-symbols-outlined">step</i> to just forward one tick
           |* <i class="material-symbols-outlined">clear</i> to clear the grid (and stop)
           |
           |Try drawing a few patterns on the grid (click cells to toggle them), hit play, and see what happens.
           |
           |Just from these two rules, we're going to find that the game gets hard to predict where it's going to go.
           |
           |""".stripMargin),
        () => VNodeStage.card(
          <.div(LifeGame("demo", 15, 15)())
        ),
      ),

      VNodeStage.twoColumn("Repeating patterns")(() => markdown.div(
         s"""
           |Others have curious repeating patterns
           |
           |Hit play on this, and you'll see 
           |
           |* an oscillator oscillating on the right
           |* a `glider` gliding down the page
           |* a tetronimo that gradually produces four oscillators
           |* a `spaceship` in the bottom right that slowly drifts left until it collides with an oscillator
           |
           |
           |""".stripMargin),
        () => VNodeStage.card(
          <.div(LifeGame("demo", 25, 25)(
            """|.........................                             
               |....#..............###...
               |..#.#....................
               |...##....................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |....#...................
               |...###
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |...................#..#..
               |..................#......
               |..................#...#..
               |..................####...
               |.........................
               |""".stripMargin
          ))
        ),
      ),

      VNodeStage.twoColumn("Will it stabilise?")(() => markdown.div(
         s"""
           |For others... well, working out if they'll stop being chaotic can take a while
           |
           |Take this little "F-pentomino". Except that it runs out of room, it'd take 4 minutes (more than 1,000 ticks) to stabilise..
           |
           |""".stripMargin),
        () => VNodeStage.card(
          <.div(LifeGame("demo", 50, 50)(
            """|.........................                             
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.........................
               |.......................##
               |......................##
               |.......................#.
               |.........................
               |""".stripMargin
          ))
        ),
      ),


    )),

)
