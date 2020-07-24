package canvasland
import org.scalajs.dom.CanvasRenderingContext2D

import scala.scalajs.js
import scala.scalajs.js.JSON

class LunarLanderSim(setup: LunarLanderSim => Unit) extends MatterSim {

  import MatterSim._

  /** Divisor when drawing */
  var scale = 10.0

  /** The maximum force of the main thruster */
  var maxThrust = 0.05

  var maxRotationThrust = 0.2

  object Lander extends Robot {

    private var _thrust:Double = 0
    private var _rotationThrust:Double = 0

    def setThrust(t:Double):Unit = {
      _thrust = Math.min(Math.max(0, t), 1) * maxThrust
    }

    def setTurn(t:Double):Unit = {
      _rotationThrust = Math.min(Math.max(-1, t), 1) * maxRotationThrust
    }

    def getAngularVelocity() = body.angularVelocity

    val body = MatterSim.Bodies.fromVertices(0, 0,
      js.Array(
        MatterSim.Vector.create(-100, 100),
        MatterSim.Vector.create(0, -150),
        MatterSim.Vector.create(100, 100),
      )
    )

    override def functions(): Seq[(String, Seq[String], js.Function)] = Seq(
      ("setThrust", Seq("number"), setThrust _),
      ("setTurnThrust", Seq("number"), setTurn _),
      ("getAngularVelocity", Seq.empty, getAngularVelocity _),
      ("getX", Seq.empty, () => body.position.x),
      ("getY", Seq.empty, () => body.position.y)


    )

    /** Graphics position */
    override def x: Double = body.position.x.asInstanceOf[Double] / scale

    /** Graphics position */
    override def y: Double = body.position.y.asInstanceOf[Double] / scale
    def angle: Double = body.angle.asInstanceOf[Double]

    private def vertices = body.vertices.asInstanceOf[js.Array[MatterSim.Vector]]

    private def localVertices = {
      for { v <- vertices } yield {
        Vector.sub(v, body.position).asInstanceOf[Vector]
      }
    }

    val shipFill = "blue"
    val thrustFill = "orange"

    override def draw(ctx: CanvasRenderingContext2D): Unit = {
      ctx.fillStyle = shipFill
      ctx.beginPath()
      for {
        v <- localVertices
      } {
        ctx.lineTo(v.x / scale, v.y / scale)
      }
      ctx.fill()

      ctx.rotate(angle)
      ctx.strokeStyle = thrustFill
      ctx.beginPath()
      ctx.moveTo(0, 9)
      ctx.lineTo(0, 9 + (10 * _thrust / maxThrust))
      ctx.stroke()
    }

    override def reset(): Unit = {
      Body.setPosition(body, MatterSim.Vector.create(0, 0))
      Body.setVelocity(body, MatterSim.Vector.create(0, 0))
      Body.rotate(body, -body.angle)
      Body.setAngularVelocity(body, 0)

      setup(LunarLanderSim.this)
    }

    def setPosition(x:Double, y:Double):Unit = {
      MatterSim.Body.setPosition(body, MatterSim.Vector.create(x, y))
    }

    override def step(c: CanvasLand): Unit = {
      body.torque = _rotationThrust

      // apply thrust forces
      val Vec2(thrustX, thrustY) = Vec2(0, -_thrust).rotate(angle)
      MatterSim.Body.applyForce(body, body.position, Vector.create(thrustX, thrustY))
    }
  }

  MatterSim.World.add(world, Lander.body)


}
