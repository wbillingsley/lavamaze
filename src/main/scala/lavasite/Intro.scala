package lavasite

import com.wbillingsley.veautiful.html.{<, ^}
import lavasite.templates.FrontPage

object Intro {

  val frontPage = new FrontPage(
    <.div(),
    <.div(^.cls := "lead",
      Common.markdown(
        """
          | # Lava Maze
          |
          | Lava Maze is a programmable game environment by [Will Billingsley](https://www.wbillingsley.com),
          | used for outreach in learn-to-code activities.
          |
          | It's designed to work with JavaScript, programmed either via raw JavaScript or via my blocks
          | programming language.
          |
          | This site contains the code of the game environment, and also works as a little test site for trying out
          | some of the functionality.
          |
          |""".stripMargin
      ),
      <.a(^.href := DeckRoute("impossibleThings", 0).path, "impossible")
    ),
    Seq(
    )
  )

}
