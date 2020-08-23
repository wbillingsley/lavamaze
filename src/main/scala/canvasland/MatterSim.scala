package canvasland

import scala.scalajs.js

class MatterSim extends Steppable {

  val engine = MatterSim.Engine.create()

  val world = engine.world

  override def step(c: CanvasLand): Unit = {
    MatterSim.Engine.update(engine)
  }

}

object MatterSim {

  /** The matter.js library, assumed already to be loaded */
  val Matter:js.Dynamic = js.Dynamic.global.Matter

  val Engine:js.Dynamic = Matter.Engine

  val Bodies:js.Dynamic = Matter.Bodies

  val Body:js.Dynamic = Matter.Body

  val World:js.Dynamic = Matter.World

  val Vector:js.Dynamic = Matter.Vector

  val Events:js.Dynamic = Matter.Events

  @js.native
  trait Vector extends js.Object {
    var x:Double = js.native
    var y:Double = js.native
  }

}
