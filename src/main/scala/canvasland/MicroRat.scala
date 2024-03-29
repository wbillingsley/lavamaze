package canvasland
import canvasland.MicroRat.{ONE_TILE, ONE_TILE_PIXELS}
import lavamaze.WallTile.image
import org.scalajs.dom.{CanvasRenderingContext2D, html}

import scala.collection.mutable
import scala.concurrent.Promise
import scala.scalajs.js

/**
 * Part-way between Micromouse and the Bilby competition. It uses a physical maze (like Micromouse) but is
 * tile-based rather than wall-based for simpler mapping (like the Bilby competition)
 */
class MicroRat(start:(Int, Int) = (0, 0), dimensions:(Int, Int) = (10, 10))(onFirst: MicroRat => Unit, onReset: MicroRat => Unit) extends MatterSim {

  sealed trait Tile {
    def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit
  }

  case object WallTile extends Tile {
    private val imageP = Promise[html.Element]
    val imageFuture = imageP.future
    private val image = lavamaze.loadImage("wall.png")
    image.onload = (_) => imageP.success(image)

    override def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit = {
      ctx.drawImage(image, 0, 0, 64, 64, x, y, 64, 64)
    }
  }

  case object FloorTile extends Tile {
    private val imageP = Promise[html.Element]
    val imageFuture = imageP.future
    private val image = lavamaze.loadImage("floor.png")
    image.onload = (_) => imageP.success(image)

    override def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit = {
      ctx.drawImage(image, 0, 0, 64, 64, x, y, 64, 64)
    }
  }

  private val (tilesWide, tilesHigh) = dimensions
  private val cells:Seq[Array[Tile]] = for {
    y <- 0 until tilesHigh
  } yield Array.fill[Tile](tilesWide)(FloorTile)

  def setTile(x:Int, y:Int, t:Tile):Unit = {
    cells(y)(x) = t
  }

  def loadMazeFromString(s:String):Unit = {
    MicroRat.process(this, s)
  }

  /** Paints the maze onto the canvas */
  def paintCanvas(cl:CanvasLand): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    for {
      _ <- WallTile.imageFuture
      _ <- FloorTile.imageFuture
    } {
      println("P")
      cl.withCanvasContext { ctx =>
        for {
          (row, y) <- cells.iterator.zipWithIndex
          (cell, x) <- row.iterator.zipWithIndex
        } {
          ctx.save()
          ctx.translate(x * ONE_TILE_PIXELS, y * ONE_TILE_PIXELS)
          cell.paint(0, 0, 0, ctx)
          ctx.restore()
        }
      }
    }
  }

  object Goal extends Mob {
    var tx = 5
    var ty = 5

    private def image = lavamaze.Goal.image

    def px:Double = tx * ONE_TILE + ONE_TILE / 2
    def py:Double = ty * ONE_TILE + ONE_TILE / 2

    override def x: Double = MicroRat.simToPixels(px)
    override def y: Double = MicroRat.simToPixels(py)

    var tick = 0

    override def draw(ctx: CanvasRenderingContext2D): Unit = {
//      ctx.clearRect(-ONE_TILE/2, -ONE_TILE/2, ONE_TILE, ONE_TILE)
      ctx.drawImage(image, 64 * ((tick / 60) % 4), 0, 64, 64, -ONE_TILE/2, -ONE_TILE/2, ONE_TILE, ONE_TILE)
    }

    override def reset(): Unit = {
      tick = 0
    }

    override def step(c: CanvasLand): Unit = {
      tick = (tick + 1) % 240
    }
  }

  /**
   * A spherical robot that bumps into walls to detect them
   */
  class Bumper(var startX:Int = start._1, var startY:Int = start._2) extends Robot {

    val radius = 0.25 * MicroRat.ONE_TILE

    val radiusPx = MicroRat.simToPixels(radius)

    def altBody = {
      MatterSim.Bodies.fromVertices(0, 0,
        js.Array(
          MatterSim.Vector.create(-100, 100),
          MatterSim.Vector.create(0, -150),
          MatterSim.Vector.create(100, 100),
        )
      )
    }

    private var _collisionIndicator = false

    def isCollisionDetected():Boolean = _collisionIndicator

    def resetCollisionDetector():Unit = _collisionIndicator = false

    val body = {
      val b = MatterSim.Bodies.circle(0, 0, radius)
      b.frictionAir = 0.1

      MatterSim.Events.on(engine, "collisionStart", (evt:js.Dynamic) => {
        val pairs = evt.pairs.asInstanceOf[js.Array[js.Dynamic]]
        println("collision!")

        for { idx <- 0 until pairs.length } {
          if (pairs(idx).bodyA == b || pairs(idx).bodyB == b) {
            _collisionIndicator = true
          }
        }
      })

      b
    }

    val wheelX = 0.5 * radius

    private var _leftSpeed = 0d
    private var _rightSpeed = 0d

    private val motorStrength = 0.0005


    def setLeftSpeed(s:Double):Unit = {
      _leftSpeed = Math.min(Math.max(-1, s), 1)
    }

    def setRightSpeed(s:Double):Unit = {
      _rightSpeed = Math.min(Math.max(-1, s), 1)
    }

    override def functions(): Seq[(String, Seq[String], js.Function)] = Seq(
      ("setLeftPower", Seq("Number"), setLeftSpeed _),
      ("setRightPower", Seq("Number"), setRightSpeed _),
      ("setLightColour", Seq("String"), (s:String) => lightFill = s),
      ("isCollisionDetected", Seq(), isCollisionDetected _),
      ("clearCollision", Seq(), resetCollisionDetector _),
      ("getX", Seq.empty, () => px),
      ("getY", Seq.empty, () => py),
      ("getVelocityX", Seq.empty, () => vx),
      ("getVelocityY", Seq.empty, () => vy),
      ("getHeading", Seq.empty, () => angle),
      ("getAngularVelocity", Seq.empty, () => angularVelocity),
      ("getGoalX", Seq.empty, () => Goal.px),
      ("getGoalY", Seq.empty, () => Goal.py),
      ("getGoalTileX", Seq.empty, () => Goal.tx),
      ("getGoalTileY", Seq.empty, () => Goal.ty),
      ("mazeColumnCount", Seq.empty, () => tilesWide),
      ("mazeRowCount", Seq.empty, () => tilesHigh),
    )

    override def x: Double = MicroRat.simToPixels(px)
    override def y: Double = MicroRat.simToPixels(py)

    def px: Double = body.position.x.asInstanceOf[Double]
    def py: Double = body.position.y.asInstanceOf[Double]
    def vx: Double = body.velocity.x.asInstanceOf[Double]
    def vy: Double = body.velocity.y.asInstanceOf[Double]
    def angularVelocity: Double = body.angularVelocity.asInstanceOf[Double]

    def setPosition(x:Double, y:Double):Unit = {
      MatterSim.Body.setPosition(body, MatterSim.Vector.create(x, y))
    }

    def angle: Double = {
      body.angle.asInstanceOf[Double]
    }

    val bodyFill = "rgba(180, 180, 255, 0.7)"
    val wheelFill = "rgba(60, 60, 60, 0.7)"
    var lightFill = "rgba(255, 180, 80, 0.7)"

    private val noCollisionFill = "rgba(60, 60, 255, 0.7)"
    private val collisionFill = "rgba(255, 60, 60, 0.7)"

    private val wheelActiveFill = "rgba(60, 255, 60, 0.7)"

    override def draw(ctx: CanvasRenderingContext2D): Unit = {
      ctx.rotate(angle)

      ctx.drawImage(MicroRat.sphereBotImg, 0, 0, 64, 64, -radiusPx, -radiusPx, 2 * radiusPx, 2 * radiusPx)

      ctx.strokeStyle = "rgba(60, 60, 60, 0.7)"
      ctx.beginPath()
      ctx.arc(0, 0, radiusPx, 0, 2 * Math.PI)
      ctx.stroke()

      ctx.fillStyle = wheelFill
      ctx.beginPath()
      ctx.fillRect(-7, -wheelX - 2, 14, 4)
      ctx.fillRect(-7, wheelX - 2, 14, 4)

      ctx.fillStyle = wheelActiveFill
      if (_leftSpeed > 0.05) {
        ctx.fillRect(0, -wheelX - 2, 7, 4)
      } else if(_leftSpeed < -0.05) {
        ctx.fillRect(-7, -wheelX - 2, 7, 4)
      }
      if (_rightSpeed > 0.05) {
        ctx.fillRect(0, wheelX - 2, 7, 4)
      } else if(_rightSpeed < -0.05) {
        ctx.fillRect(-7, wheelX - 2, 7, 4)
      }

      ctx.fillStyle = lightFill
      ctx.beginPath()
      ctx.arc(-10, 0, 5, 0, 2 * Math.PI)
      ctx.fill()

      ctx.fillStyle = if (_collisionIndicator) collisionFill else noCollisionFill
      ctx.beginPath()
      ctx.arc(0, 0, 15, -1, 1)
      ctx.fill()
    }

    /** Put the robot back at the start */
    override def reset(): Unit = {
      _collisionIndicator = false
      MatterSim.Body.setPosition(body, MatterSim.Vector.create(0, 0))
      MatterSim.Body.setVelocity(body, MatterSim.Vector.create(0, 0))
      MatterSim.Body.rotate(body, -body.angle)
      MatterSim.Body.setAngularVelocity(body, 0)

      _leftSpeed = 0
      _rightSpeed = 0

      setPosition(startX * ONE_TILE + ONE_TILE / 2, startY * ONE_TILE + ONE_TILE / 2)
      MicroRat.this.onReset(MicroRat.this)
    }

    def applyForce(relativePos:Vec2, force:Vec2):Unit = {
      val pos = MatterSim.Vector.add(body.position, MatterSim.Vector.create(relativePos.x, relativePos.y))
      val f = MatterSim.Vector.create(force.x, force.y)

      MatterSim.Body.applyForce(body, pos, f)
    }

    override def step(c: CanvasLand): Unit = {
      // apply thrust forces
      val leftForce = Vec2(_leftSpeed * motorStrength, 0).rotate(angle)
      val leftPos = Vec2(0, -wheelX).rotate(angle)
      val rightForce = Vec2(_rightSpeed * motorStrength, 0).rotate(angle)
      val rightPos = Vec2(0, wheelX).rotate(angle)

      applyForce(leftPos, leftForce)
      applyForce(rightPos, rightForce)
    }
  }



  val robot = new Bumper()

  // Do any one-time setup.
  onFirst(this)
  world.gravity.scale = 0

  MatterSim.World.add(world, robot.body)

  def addStaticBlock(x:Double, y:Double, w:Double, h:Double):Unit = {
    val b = MatterSim.Bodies.rectangle(x, y, w, h)
    MatterSim.Body.setStatic(b, true)
    MatterSim.World.add(world, b)
  }

  def addStaticGridBlock(x:Int, y:Int)(w:Int, h:Int):Unit = {
    val xStart = x * ONE_TILE
    val width = w.toDouble * ONE_TILE
    val yStart = y * ONE_TILE
    val height = h.toDouble * ONE_TILE
    addStaticBlock(xStart + width / 2, yStart + height / 2, width, height)
  }

  // Add objects for any wall tiles
  for {
    (row, y) <- cells.iterator.zipWithIndex
    (cell, x) <- row.iterator.zipWithIndex
  } {
    cell match {
      case WallTile =>
        addStaticGridBlock(x, y)(1, 1)
      case _ => //skip
    }
  }

  // Add sides
  addStaticGridBlock(-1, 0)(1, tilesHigh)
  addStaticGridBlock(tilesWide, 0)(1, tilesHigh)
  addStaticGridBlock(0, -1)(tilesWide, 1)
  addStaticGridBlock(0, tilesHigh)(tilesWide, 1)

}

object MicroRat {

  val ONE_TILE = 64d

  val ONE_TILE_PIXELS = 64

  val sphereBotImg = lavamaze.loadImage("spherebot.png")

  def simToPixels(d:Double):Double = d * scale

  def pixelsToSim(d:Double):Double = d / scale

  /** Scales sim dimensions to pixels */
  val scale = ONE_TILE_PIXELS / ONE_TILE

  private val actions:mutable.Map[Char, (MicroRat, Int, Int) => Unit] = mutable.Map(
    '.' -> { case (m, x, y) => m.setTile(x, y, m.FloorTile) },
    '#' -> { case (m, x, y) => m.setTile(x, y, m.WallTile) },
  )

  def process(m:MicroRat, s:String):Unit = {
    for {
      (line, y) <- s.linesIterator.zipWithIndex if y < m.tilesHigh
      (char, x) <- line.zipWithIndex if x < m.tilesWide
    } {
      if (actions.contains(char)) {
        actions(char)(m, x, y)
      }
    }
  }



}
