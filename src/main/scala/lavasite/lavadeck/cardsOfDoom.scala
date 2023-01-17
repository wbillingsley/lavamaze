package lavasite.lavadeck

import com.wbillingsley.veautiful.html.*
import com.wbillingsley.veautiful.templates.*
import VNodeStage.card

import scala.scalajs.js
import scala.util.Random

import lavasite.{markdown, styleSuite}
import com.wbillingsley.veautiful.templates.Challenge.Completion


val cardsOfDoomStyling = Styling(
  """|
     |""".stripMargin
).modifiedBy(
  " .cod-card" -> 
    """|    display: inline-flex;
       |    padding: 20px;
       |    margin: 10px;
       |    width: 80px;
       |    height: 100px;
       |    font-family: "Michroma", sans-serif;
       |    font-weight: bolder;
       |    text-align: center;
       |    border: 1px solid rebeccapurple;
       |    border-radius: 5px;
       |    background-color: #0d2d48;
       |    background-image: linear-gradient(white 2px, transparent 2px), linear-gradient(90deg, white 2px, transparent 2px), linear-gradient(rgba(255,255,255,.3) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.3) 1px, transparent 1px);
       |    background-size: 100px 100px, 100px 100px, 20px 20px, 20px 20px;
       |    background-position: -2px -2px, -2px -2px, -1px -1px, -1px -1px;
       |    color: white;
       |    box-shadow: 10px 5px 5px #4e4d4d;
       |""".stripMargin,
  " .cod-card.cod-card-10" -> "background-color: #99461b;",
  " .cod-card.cod-card-11" -> "background-color: #99461b;",
  " .cod-card.cod-card-12" -> "background-color: #99461b;",
  " .cod-card.cod-card-0" -> "background-color: #64000c;",
).register()

object StageCardsOfDoom extends Challenge.Stage {

  override def completion: Completion = Challenge.Open
  override def kind: String = "text"

  var lost = 0
  var reachedGoal = false

  def render = <.div(
    Challenge.split(
      textColumn(
        markdown.div(
          """|## An interlude - Cards of Doom.
             |
             |Let's play a game called Cards of Doom.
             |We start with 13 cards. On your turn, you can take 1, 2, or 3 cards. Then it's my turn and I take 1, 2, or 3 cards. And so on.
             |
             |The person who picks up the last card (the Card of Doom) loses.
             |
             |I'll go first. There is an algorithm that will let the *second player* always win.
             |(But if you make a mistake, I'll do it to you and win.)
             |
             |To solve this puzzle, you have to win a round... probably by spotting the pattern from losing a few first.
             |
             |""".stripMargin
        )
      )
    )(
      textColumn(
        card(<.div(
          <.div(
            if (CardsOfDoom.remaining > 0) {
              if (CardsOfDoom.remaining > 1) {
                s"There are ${CardsOfDoom.remaining} cards remaining."
              } else {
                "There is 1 card remaining."
              }
            } else {
              if (CardsOfDoom.iWin) {
                <.div(
                  <.p("Oh no! You took the Card of Doom! I win this round!"),
                  <.button(^.cls := "btn btn-outline-primary", "Play again", ^.onClick --> CardsOfDoom.reset(false))
                )
              } else {
                <.div(
                  <.p("Congratulations! I took the Card of Doom! You win!"),
                  <.button(^.cls := "btn btn-outline-primary", "Play again", ^.onClick --> CardsOfDoom.reset(true))
                )
              }
            }
          ),
          <.div(^.cls := cardsOfDoomStyling,
            for (i <- 0 until CardsOfDoom.remaining) yield {
              <.div(^.cls := s"cod-card cod-card-$i",
                i match {
                  case 0 => "A"
                  case 10 => "J"
                  case 11 => "Q"
                  case 12 => "K"
                  case _ => (i+1).toString
                }
              )
            }
          ),
          <.div(
            if (CardsOfDoom.remaining > 0) {
              if (CardsOfDoom.myTurn) {
                <.div(
                  <.p(s"It's my turn. I will take ${ CardsOfDoom.iWillTake } cards"),
                  <.button(^.cls := "btn btn-outline-primary", "Play my turn", ^.onClick --> CardsOfDoom.playMyturn() )
                )
              } else {
                val max = if (CardsOfDoom.remaining > 3) 3 else CardsOfDoom.remaining

                <.div(
                  <.p("It's your turn. How many cards will you take?"),
                  for { i <- 1 to max } yield {
                    <.button(^.cls := "btn btn-outline-primary", s"Take $i", ^.onClick --> CardsOfDoom.play(i))
                  }
                )
              }
            } else {
              <.div()
            }
          )
        )
      )
    )

  ))

  object CardsOfDoom {

    var remaining = 13
    var myTurn = true

    var iWillTake = choose()

    def choose():Int = {
      if (remaining > 0) {
        val mod = (remaining - 1) % 4
        val max = if (remaining > 3) 3 else remaining
        if (mod > 0) mod else Random.nextInt(max) + 1
      } else 0
    }

    def play(i:Int):Unit = {
      if (!myTurn && i > 0 && i < 4 && i <= remaining) {
        remaining = remaining - i
        myTurn = true
        iWillTake = choose()

        if (iWin) lost = lost + 1

        rerender()
      }
    }

    def playMyturn():Unit = {
      if (myTurn && remaining > 0) {
        remaining = remaining - iWillTake
        myTurn = false

        if (youWin) reachedGoal = true

        rerender()
      }
    }

    def iWin = remaining == 0 && myTurn

    def youWin = remaining == 0 && !myTurn

    def reset(won:Boolean) = {
      remaining = 13
      myTurn = true
      iWillTake = choose()
      rerender()
    }

  }
}
