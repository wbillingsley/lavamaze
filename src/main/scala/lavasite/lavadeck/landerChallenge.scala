package lavasite.lavadeck

import com.wbillingsley.veautiful.html.*
import com.wbillingsley.veautiful.doctacular.Challenge
import Challenge.{Completion, Level}

import canvasland.LunarLanderSim
import coderunner.{JSCodable, PrefabCodable, LoggingPrefabCodable}
import lavamaze.{FloorTile, Goal, Maze, Overlay}
import org.scalajs.dom.{Element, Node}

import lavasite.{markdown, styleSuite}
import scala.util.Random

object LanderChallenge {

  class LanderStage(md:String, lls: LunarLanderSim) extends Challenge.Stage {

    lls.Lander.showState = true

    override def completion: Completion = Challenge.Open
    override def kind = "exercise"

    private val codable = JSCodable(lls.canvasLand)(tilesMode = false)
    override protected def render = textColumn(
      <.div(^.cls := "lead", ^.attr("style") := "height: 200px", markdown.div(md)), codable
    )
    
  }

  class TextBelowLanderStage(top:VHtmlContent, lls: LunarLanderSim) extends Challenge.Stage {

    lls.Lander.showState = true

    override def completion: Completion = Challenge.Open
    override def kind = "exercise"

    private val codable = JSCodable(lls.canvasLand)(tilesMode = false)
    override protected def render = textColumn(
      codable, <.div(^.cls := "lead", ^.attr("style") := "height: 200px", top), 
    )
    
  }

  val levels = Seq(
    Level("Lunar lander", Seq(
      LanderStage(
        """## Introducing our lunar lander
          |
          |Here we have our lander. It has a main thruster and four attitude control jets (two on each side). We're
          |going to need to get it across the landscape to land on a small green target... but let's just play with it
          |first.
          |
          |Call showState(), run the sim, turn the thruster on, etc...
          |""".stripMargin,
        LunarLanderSim("lander")(onReset = { sim =>
          sim.world.gravity.y = 0.16

          sim.Lander.setPosition(1000, 500)
          sim.Lander.angle = Random.nextDouble() * 2 * Math.PI
        })
      ),

      LanderStage(
        """## Land us gently...
          |
          |The lander is lined up over the pad. All you've got to do is control the main thruster to give us a gentle landing.
          |
          |The easy way is "bang bang control": if we're falling too fast, turn the thruster on. If not; turn it off again.
          |(To save your blushes, the ship won't explode if it lands too fast, but the challenge is to land it with a `vy` of 4 or less)
          |
          |""".stripMargin,
        LunarLanderSim("lander")(onReset = { sim =>
          sim.world.gravity.y = 0.16

          sim.Lander.setPosition(4900, 500)
        })
      ),

      LanderStage(
        """## Right the ship!
          |
          |I've turned gravity off. Your task: get the ship pointing the right way up!
          |This is going to be trickier, because you're trying to control the angle but you can only control the angular acceleration with the thrusters.
          |
          |There's a little trick that makes it easier. "If *in a few seconds' time* I'll be facing too far left, thrust right"
          |
          |PS. `Math.abs(-3)` and `Math.abs(3)` are both 3. Useful if you're asking "am I close enough to upright that I can stop?"
          |
          |""".stripMargin,
        LunarLanderSim("lander")(onReset = { sim =>
          sim.world.gravity.y = 0

          sim.Lander.setPosition(1000, 500)
          sim.Lander.angle = Random.nextDouble() * Math.PI + Math.PI / 2
        })
      ),

      LanderStage(
        """## Right the ship! And get us over the pad!
          |
          |Gravity's still off and the set-up is (roughly) the same. But this time your task is to right the ship and then
          |manoeuvre it sideways to get it over the landing pad at `x = 4900`.
          |
          |Again, the trick with controlling it is to do ask yourself if you'll be close enough to the right location *in a moment's time*
          |
          |""".stripMargin,
        LunarLanderSim("lander")(onReset = { sim =>
          sim.world.gravity.y = 0

          sim.Lander.setPosition(1000, 500)
          sim.Lander.angle = Random.nextDouble() * Math.PI + Math.PI / 2
        })
      ),

      LanderStage(
        """## The ultimate challenge!
          |
          |Gravity's on! The ship's the wrong way up! We're nowhere near the pad! And you need to land us with `Vy < 5`
          |
          |i.e.: put your code from the previous stages into functions. Then: right the ship, lift back up to an ok height, move it sideways, then land it gently.
          |
          |You might find when "landing it gently", you're not exactly upright so will drift sideways a bit. 
          |""".stripMargin,
        LunarLanderSim("lander")(onReset = { sim =>
          sim.world.gravity.y = 0.16

          sim.Lander.setPosition(1000, 500)
          sim.Lander.angle = Random.nextDouble() * Math.PI + Math.PI / 2
        })
      )

      
    ),

      
    )
  )


}