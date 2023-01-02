package canvasland
import coderunner.Codable
import com.wbillingsley.veautiful.html.{VHtmlComponent, VHtmlElement}
import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, Node}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.Random


/**
 * Lunar Lander as a Codable
 */
class LunarLanderSim(name:String, dimensions:(Int, Int) = (640, 480), lz: (Int, Int, Int) = (480, 512, 420))(
  onFirst: LunarLanderSim => Unit = { _ => }, onReset: LunarLanderSim => Unit = { _ => }) extends MatterSim {

  import MatterSim._

  /** Divisor when drawing */
  var scale = 10.0

  /** Extract the width and height of the field */
  val (width, height) = dimensions

  /** The maximum force of the main thruster */
  var maxThrust = 0.05

  /** The max strength of the side thrusters */
  var maxSideThrust = 0.02d

  /** How much fuel there is */
  var fuel = 100d

    /** Land heights. Currently, these are held in pixel coordinates. */
  val landHeights:mutable.Buffer[Int] = mutable.Buffer(landGeneration(height * 4/5, height/5, lz=lz):_*)


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

    private var _tlThrust:Double = 0
    private var _trThrust:Double = 0
    private var _blThrust:Double = 0
    private var _brThrust:Double = 0

    private var tickTimer = TickTimer(0)

    def setThrust(t:Double):Unit = {
      _thrust = Math.min(Math.max(0, t), 1) * maxThrust
    }

    def setTurn(t:Double):Unit = {
      if (t >= 0) {
        _blThrust = 0
        _trThrust = 0
        _tlThrust = Math.min(t, 1) * maxSideThrust
        _brThrust = Math.min(t, 1) * maxSideThrust
      } else {
        _brThrust = 0
        _tlThrust = 0
        _trThrust = Math.min(-t, 1) * maxSideThrust
        _blThrust = Math.min(-t, 1) * maxSideThrust
      }
    }

    def setSidle(t:Double):Unit = {
      if (t >= 0) {
        _brThrust = 0
        _trThrust = 0
        _blThrust = Math.min(t, 1) * maxSideThrust
        _tlThrust = Math.min(t, 1) * maxSideThrust
      } else {
        _blThrust = 0
        _tlThrust = 0
        _brThrust = Math.min(-t, 1) * maxSideThrust
        _trThrust = Math.min(-t, 1) * maxSideThrust
      }
    }

    def waitTicks(ticks:Int):Future[Unit] = {
      tickTimer = TickTimer(ticks)
      tickTimer.future
    }

    def getAngularVelocity() = body.angularVelocity.asInstanceOf[Double]

    /** Lander */
    val body = MatterSim.bodyFromVertices(Seq(
      -100d -> 100d, -50d -> -100d, 50d -> -100d, 100d -> 100d
    ))
    body.frictionAir = 0d

    override def functions(): Seq[(String, Seq[String], js.Function)] = {
      import scala.scalajs.js.JSConverters._
      import scala.concurrent.ExecutionContext.Implicits.global


      Seq(
        ("setThrust", Seq("number"), setThrust _),
        ("setTurnThrust", Seq("number"), setTurn _),
        ("setSideThrust", Seq("number"), setSidle _),
        ("getX", Seq.empty, () => body.position.x),
        ("getY", Seq.empty, () => body.position.y),
        ("getVx", Seq.empty, () => body.velocity.x),
        ("getVy", Seq.empty, () => body.velocity.y),
        ("getAngle", Seq.empty, () => angle),
        ("getAngularVelocity", Seq.empty, getAngularVelocity _),
        ("showState", Seq.empty, () => { showState = true }),
        ("wait", Seq.empty, (t:Double) => waitTicks(t.toInt).toJSPromise),
      )
    }

    def angle_=(a:Double):Unit = {
      Matter.Body.setAngle(body, a)
    }

    /** Graphics position */
    override def x: Double = body.position.x.asInstanceOf[Double] / scale

    /** Graphics position */
    override def y: Double = body.position.y.asInstanceOf[Double] / scale

    /** Angle of the ship */
    def angle: Double = body.angle.asInstanceOf[Double]

    var showState:Boolean = false

    val shipFill = "lightGray"
    val thrustFill = "orange"

    override def draw(ctx: CanvasRenderingContext2D): Unit = {
      ctx.fillStyle = shipFill
      ctx.strokeStyle = shipFill

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

      def drawSideThruster(x:Double, y:Double, disp:Double): Unit = {
        if (disp != 0) {
          ctx.fillStyle = thrustFill
          ctx.beginPath()
          ctx.moveTo(x, y - 1)
          ctx.lineTo(x + disp, y)
          ctx.lineTo(x, y + 1)
          ctx.lineTo(x, y - 1)
          ctx.fill()
        }
      }

      drawSideThruster(-10, -10, -8 * _tlThrust / maxSideThrust)
      drawSideThruster(10, -10, 8 * _trThrust / maxSideThrust)
      drawSideThruster(-10, 10, -8 * _blThrust / maxSideThrust)
      drawSideThruster(10, 10, 8 * _brThrust / maxSideThrust)

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

      _tlThrust = 0
      _trThrust = 0
      _blThrust = 0
      _brThrust = 0
      _thrust = 0

      tickTimer.interrupt()

      onReset(LunarLanderSim.this)
    }

    def setPosition(x:Double, y:Double):Unit = {
      MatterSim.Body.setPosition(body, MatterSim.Vector.create(x, y))
    }

    override def step(c: CanvasLand): Unit = {

      def matterForce(v:Vec2):js.Dynamic = {
        val Vec2(x, y) = v
        Vector.create(x, y)
      }

      def bodyOffset(v:Vec2):js.Dynamic = {
        val Vec2(x, y) = v
        Vector.add(body.position, Vector.create(x, y))
      }


      // apply thrust forces
      val Vec2(thrustX, thrustY) = Vec2(0, -_thrust).rotate(angle)
      MatterSim.Body.applyForce(body, body.position, Vector.create(thrustX, thrustY))

      MatterSim.Body.applyForce(body, bodyOffset(Vec2(0, -100).rotate(angle)), matterForce(Vec2(_tlThrust, 0).rotate(angle)))
      MatterSim.Body.applyForce(body, bodyOffset(Vec2(0, -100).rotate(angle)), matterForce(Vec2(- _trThrust, 0).rotate(angle)))
      MatterSim.Body.applyForce(body, bodyOffset(Vec2(0, 100).rotate(angle)), matterForce(Vec2(_blThrust, 0).rotate(angle)))
      MatterSim.Body.applyForce(body, bodyOffset(Vec2(0, 100).rotate(angle)), matterForce(Vec2(- _brThrust, 0).rotate(angle)))

      if (!tickTimer.complete) {
        tickTimer.tick()
      }
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

  /** A function that generates land heights. LZ is the landing zome */
  def landGeneration(mid:Int, range:Int, steps:Int = 20, lz:(Int, Int, Int)):Seq[Int] = {
    val (x1, x2, h) = lz

    for { x <- 0 to width by width/steps } yield {
      if (x1 <= x && x <= x2) h else mid + Random.nextInt(range) - range / 2
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

      ctx.strokeStyle = "green"
      ctx.lineWidth = 3
      ctx.beginPath()
      val (x1, x2, h) = lz
      ctx.moveTo(x1, h)
      ctx.lineTo(x2, h)
      ctx.stroke()
    })


    canvasLand.addSteppable(this)
  })



}
