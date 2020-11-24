package canvasland

import org.scalajs.dom

import scala.scalajs.js

class MatterSim extends Steppable {

  val engine:js.Dynamic = MatterSim.Engine.create()

  val world:js.Dynamic = engine.world

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

  val Render:js.Dynamic = Matter.Render

  @js.native
  trait Vector extends js.Object {
    var x:Double = js.native
    var y:Double = js.native
  }


  def renderBody(body:js.Dynamic, ctx:dom.CanvasRenderingContext2D, scale:Double = 1d): Unit = {
    def vertices = body.vertices.asInstanceOf[js.Array[MatterSim.Vector]]

    ctx.beginPath()
    for { v <- vertices } {
      ctx.lineTo(v.x / scale, v.y / scale)
    }
    ctx.fill()
    ctx.stroke()
  }

  /** Given a convex sequence of points, in clockwise order, create a Matter.js body. */
  def bodyFromVertices(vertices:Seq[(Double, Double)]):js.Dynamic = {
    val raw = js.Array(
      (vertices.map { case (x, y) => Vector.create(x, y) }):_*
    )
    val centroid = Matter.Vertices.centre(raw)
    MatterSim.Body.create(js.Dynamic.literal(
      "position" -> centroid, //MatterSim.Vector.create(cx, cy),
      "vertices" -> raw
    ))
  }

}
