package canvasland
import coderunner.Codable
import com.wbillingsley.veautiful.html.{VHtmlComponent, VHtmlNode}
import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, Node}

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.Random


/**
 * Lunar Lander as a Codable
 */
class LunarLanderSim(name:String, dimensions:(Int, Int) = (640, 480))(
  onFirst: LunarLanderSim => Unit = { _ => }, onReset: LunarLanderSim => Unit = { _ => }) extends MatterSim {

  import MatterSim._

  /** Divisor when drawing */
  var scale = 10.0

  /** Extract the width and height of the field */
  val (width, height) = dimensions

  /** The maximum force of the main thruster */
  var maxThrust = 0.05

  var maxRotationThrust = 0.2

  /** Land heights. Currently, these are held in pixel coordinates. */
  val landHeights:mutable.Buffer[Int] = mutable.Buffer(landGeneration(height * 4/5, height/5):_*)

  /** (x, y) coordinates in physics coordinates of the land */
  def landVertices:Seq[(Double, Double)] = {
    val step = (width * scale) / (landHeights.size - 1)
    println(s"width $width step $step")
    (for {
      (y, i) <- landHeights.iterator.zipWithIndex
    } yield {
      val x = i * step
      (x, y * scale)
    }).toSeq
  }

  object Lander extends Robot {

    private var _thrust:Double = 0
    private var _rotationThrust:Double = 0

    def setThrust(t:Double):Unit = {
      _thrust = Math.min(Math.max(0, t), 1) * maxThrust
    }

    def setTurn(t:Double):Unit = {
      _rotationThrust = Math.min(Math.max(-1, t), 1) * maxRotationThrust
    }

    def getAngularVelocity() = body.angularVelocity.asInstanceOf[Double]

    /** Lander */
    val body = MatterSim.bodyFromVertices(Seq(
      -100d -> 100d, -50d -> -100d, 50d -> -100d, 100d -> 100d
    ))

    override def functions(): Seq[(String, Seq[String], js.Function)] = Seq(
      ("setThrust", Seq("number"), setThrust _),
      ("setTurnThrust", Seq("number"), setTurn _),
      ("getX", Seq.empty, () => body.position.x),
      ("getY", Seq.empty, () => body.position.y),
      ("getVx", Seq.empty, () => body.velocity.x),
      ("getVy", Seq.empty, () => body.velocity.y),
      ("getAngle", Seq.empty, () => angle), 
      ("getAngularVelocity", Seq.empty, getAngularVelocity _),
      ("showState", Seq.empty, () => { showState = true })
    )

    /** Graphics position */
    override def x: Double = body.position.x.asInstanceOf[Double] / scale

    /** Graphics position */
    override def y: Double = body.position.y.asInstanceOf[Double] / scale

    /** Angle of the ship */
    def angle: Double = body.angle.asInstanceOf[Double]

    private def vertices = body.vertices.asInstanceOf[js.Array[MatterSim.Vector]]

    var showState:Boolean = false

    private def localVertices = {
      for { v <- vertices } yield {
        Vector.sub(v, body.position).asInstanceOf[Vector]
      }
    }

    val shipFill = "lightGray"
    val thrustFill = "orange"

    override def draw(ctx: CanvasRenderingContext2D): Unit = {
      ctx.fillStyle = shipFill
      ctx.strokeStyle = shipFill

      /* debug - draw bounding box
      ctx.beginPath()
      for {
        v <- localVertices
      } {
        ctx.lineTo(v.x / scale, v.y / scale)
      }
      ctx.stroke()
      */

      val x = body.position.x.asInstanceOf[Double]
      val y = body.position.y.asInstanceOf[Double]

      ctx.rotate(angle)

      ctx.beginPath()
      ctx.arc(0, -5, 7, 0, 2 * Math.PI)
      ctx.fill()
      ctx.beginPath()
      ctx.moveTo(-10, 10)
      ctx.lineTo(-5, 0)
      ctx.lineTo(5, 0)
      ctx.lineTo(10, 10)
      ctx.lineTo(5, 0)
      ctx.lineTo(3, 8)
      ctx.lineTo(-3, 8)
      ctx.lineTo(-5, 0)
      ctx.stroke()

      if (_thrust > 0) {
        ctx.fillStyle = thrustFill
        ctx.beginPath()
        ctx.moveTo(-2, 9)
        ctx.lineTo(0, 9 + (10 * _thrust / maxThrust))
        ctx.lineTo(2, 9)
        ctx.lineTo(-2, 9)
        ctx.fill()
      }

      if (showState) {
        ctx.rotate(-angle)
        ctx.fillStyle = "cyan"
        ctx.beginPath()
        ctx.fillText(s"x: ${Math.round(x)}, y: ${Math.round(y)}", 20, -12)
        ctx.fillText(f"vx: ${body.velocity.x.asInstanceOf[Double]}%1.1f, vy: ${body.velocity.y.asInstanceOf[Double]}%1.1f", 20, 0)
        ctx.fillText(f"angle: ${angle}%1.2f, angularVelocity: ${getAngularVelocity()}%1.5f", 20, 12)
        ctx.fill()
      }

    }

    override def reset(): Unit = {
      Body.setPosition(body, MatterSim.Vector.create(0, 0))
      Body.setVelocity(body, MatterSim.Vector.create(0, 0))
      Body.rotate(body, -body.angle)
      Body.setAngularVelocity(body, 0)

      onReset(LunarLanderSim.this)
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

  println(landVertices)
  /** Ground */
  val ground = {
    // The ground has to be made up of convex shapes, so we do each trapezium separately

    val pairs = landVertices.zip(landVertices.tail)
    val bottom = height * scale

    // Define the trapeziums
    for {
      ((x1, y1), (x2, y2)) <- pairs
    } yield {
      val cx = (x1 + x2) / 2d
      val cy = (bottom + (y1 + y2) / 2d) / 2d

      val slope = (y2 - y1) / (x2 - x1)

      val body = MatterSim.bodyFromVertices(Seq(x1 -> bottom, x1 -> y1, x2 -> y2, x2 -> bottom))
      MatterSim.Body.setStatic(body, true)
      MatterSim.World.add(world, body)

      body
    }
  }
  MatterSim.World.add(world, Lander.body)

  // Call the first-time initialisation function, if any
  onFirst(this)

  /** A function that generates land heights */
  def landGeneration(mid:Int, range:Int):Seq[Int] = {
    for { _ <- 0 to width by width/20 } yield {
      mid + Random.nextInt(range) - range / 2
    }
  }

  /** Internally, we use a CanvasLand for rendering */
  val canvasLand = CanvasLand(name)(dimensions, dimensions, Lander, setup = { canvasLand =>
    canvasLand.fillCanvas("#222")

    canvasLand.withCanvasContext({ ctx =>

      // draw some stars
      ctx.fillStyle = "white"
      for { _ <- 0 until 200} {
        val x = Random.nextInt(width)
        val y = Random.nextInt(height)

        ctx.beginPath()
        ctx.arc(x, y, 1, 1, Math.PI * 2)
        ctx.fill()
      }

      ctx.fillStyle = "lightgray"
      ctx.strokeStyle = "lightgray"
      ctx.lineWidth = 1
      for { groundBody <- ground } {
        MatterSim.renderBody(groundBody, ctx, scale)
      }

    })


    canvasLand.addSteppable(this)
  })



}
