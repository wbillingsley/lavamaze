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
import com.wbillingsley.veautiful.Blueprint
import org.scalajs.dom.HTMLInputElement


case class TTTWidget() extends DHtmlComponent {

  val styling = Styling("""
  |font-size: 24px;
  |font-family: monospace;
  |""".stripMargin).modifiedBy(
    " .nought" -> "background-color: lightskyblue;",
    " .cross" -> "background-color: pink;",
  ).register()

  val st = stateVariable(Map.empty[Int, Boolean])
  val turn = stateVariable(false)

  def play(n:Int) = {
    st.value = st.value.updated(n, turn.value)
    turn.value = !turn.value
  }

  override protected def render: DiffNode[org.scalajs.dom.html.Element, Node] | Blueprint[DiffNode[org.scalajs.dom.html.Element, Node]] = <.div(^.cls := styling,
    <.div(for n <- 1 to 9 yield 
        if st.value.contains(n) then 
            if st.value(n) then 
                <.button(^.cls := "cross", n.toString)
            else <.button(^.cls := "nought", n.toString)
        else <.button(^.cls := "ready", ^.on.click --> play(n), n.toString)
    ),
    <.div(<.button("Reset", ^.on.click --> { st.value = Map.empty[Int, Boolean]; turn.value = false}))

  )

}

case class TTTWidget2() extends DHtmlComponent {

  val styling = Styling("""
  |font-size: 24px;
  |font-family: monospace;
  |""".stripMargin).modifiedBy(
    " .nought" -> "background-color: lightskyblue;",
    " .cross" -> "background-color: pink;",
  ).register()

  val st = stateVariable(Map.empty[Int, Boolean])
  val turn = stateVariable(false)

  def play(n:Int) = {
    st.value = st.value.updated(n, turn.value)
    turn.value = !turn.value
  }

  override protected def render: DiffNode[org.scalajs.dom.html.Element, Node] | Blueprint[DiffNode[org.scalajs.dom.html.Element, Node]] = 
    def button(n:Int) = 
        if st.value.contains(n) then 
            if st.value(n) then 
                <.button(^.cls := "cross", n.toString)
            else <.button(^.cls := "nought", n.toString)
        else <.button(^.cls := "ready", ^.on.click --> play(n), n.toString)
    
    <.div(^.cls := styling,
        <.div(button(8), button(1), button(6)),
        <.div(button(3), button(5), button(7)),
        <.div(button(4), button(9), button(2)),       
        <.div(<.button("Reset", ^.on.click --> { st.value = Map.empty[Int, Boolean]; turn.value = false}))
    )

}

val levels2025 = Seq( 

    Challenge.Level("How do machines learn?", Seq(
        VNodeStage.hero(<.div(^.style := "padding: 5em;",
            <.h1(^.style := "font-family: 'Bungee Spice'; font-size: 96px;", "Do Try This at Home*"),
            <.p(^.style := "font-size: 60px;", "A coding and computing workshop"),
            <.p(^.style := "margin-top: 15em;", "* Ok, these are slides for an in-person workshop, but you can do most of the games we'll show at home too!")
        )),
        VNodeStage.hero(<.div(^.style := "padding: 5em;",
            <.img(^.src := "//turing.une.edu.au/~wbilling/faroutscience/farrow.png", ^.style := "float: left; margin-right: 50px;"),
            <.h1(^.style := "font-family: 'Bungee Spice'; font-size: 96px;", "Our AI Co-host"),
            <.p(^.style := "font-size: 60px;", 
                      
            "Introducing Farrow!"),
            
        )),
        VNodeStage.twoColumn("")(
        left = () => <.div(
            <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Machine learning"),
            markdown.div(
                """|*Which of my cue phrases did that sound like?*
                |
                |e.g. 
                |
                |> "Nearly useless?"
                |>
                |> "What do you mean, nearly useless?"
                |>
                |> "You called me nearly useless"
                |
                |all produce
                |
                |`NearlyUseless`
                |
                |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div(
            <.h2(^.style := "font-family: 'Bungee Spice'; font-size: 60px;", "Programming"),
            markdown.div(
                """|If you receive 
                |
                |`NearlyUseless`
                |
                |then say 
                |
                |> "You pressed the unlock button on your phone very well. I'm glad they gave me a co-host with fully working thumbs."
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
                """|# So you think you can count?
                   |
                   |Two's ~~company~~ complement
                   |
                   |When does 1 = -1?
                   |""".stripMargin
            )
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),  

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


    Challenge.Level("How do machines think?", Seq(

        VNodeStage.twoColumn("")(
        left = () => <.div(
            markdown.div(
                """|Fifteen!
                   |
                   |* Each turn, you take a number
                   |* If you get 3 numbers that add to 15, you win!
                |""".stripMargin
            ),
            TTTWidget()
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),       

         VNodeStage.twoColumn("")(
        left = () => <.div(
            markdown.div(
                """|Fifteen!
                   |
                   |* Each turn, you take a number
                   |* If you get 3 numbers that add to 15, you win!
                |""".stripMargin
            ),
            TTTWidget2()
        )(^.style := "font-size: 200%;"), 
        right = () => <.div()
        ),   

        VNodeStage.hero(
        <.div(
            <.a(^.href := "https://allpoetry.com/disobedience", "Disobediance")
        )
        ),

        VNodeStage.hero(
        <.div(
            <.img(^.src := "//turing.une.edu.au/~wbilling/faroutscience/tic-tac-toe-outcome.webp", ^.attr.style := "width: 60%;")
        )
        ),

        VNodeStage.hero(
        markdown.div(
                """|Close your eyes. Now picture an apple.
                |""".stripMargin
            ),
        ),   

        VNodeStage.hero(
        markdown.div(
                """|Close your eyes. Now picture an apple.
                |
                |What colour was the apple?
                |
                |- Red
                |- Green
                |- It didn't have a colour until I asked you what colour it was
                |- It did have a colour, you just couldn't see it
                |""".stripMargin
            ),
        ),   


        VNodeStage.hero(
            <.div(^.style := "height: 200px; width: 200px; background: red;")
        ),  

        VNodeStage.hero(
            <.div(^.style := "height: 200px; width: 200px; background: green;")
        ),  


        VNodeStage.hero(
        <.div(
            <.img(^.src := "https://2.bp.blogspot.com/-17ajatawCW4/VYITTA1NkDI/AAAAAAAAAlM/eZmy5_Uu9TQ/s1600/classvis.png", ^.attr.style := "width: 100%;")
        )
        ),   


        VNodeStage.hero(
        <.div(
            <.img(^.src := "//turing.une.edu.au/~wbilling/faroutscience/davinci2 banana.jpg", ^.attr.style := "width: 100%;")
        )
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
