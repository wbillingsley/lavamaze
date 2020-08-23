package canvasland
import canvasland.MicroRat.{ONE_TILE, ONE_TILE_PIXELS}
import lavamaze.WallTile.image
import org.scalajs.dom.CanvasRenderingContext2D

import scala.collection.mutable
import scala.scalajs.js

/**
 * Part-way between Micromouse and the Bilby competition. It uses a physical maze (like Micromouse) but is
 * tile-based rather than wall-based for simpler mapping (like the Bilby competition)
 */
class MicroRat(onFirst: MicroRat => Unit, onReset: MicroRat => Unit) extends MatterSim {

  sealed trait Tile {
    def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit
  }

  case object WallTile extends Tile {
    private val image = lavamaze.loadImage("wall.png")
    override def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit = {
      ctx.drawImage(image, 0, 0, 64, 64, x, y, 64, 64)
    }
  }

  case object FloorTile extends Tile {
    private val image = lavamaze.loadImage("floor.png")
    override def paint(layer: Int, x: Int, y: Int, ctx: CanvasRenderingContext2D): Unit = {
      ctx.drawImage(image, 0, 0, 64, 64, x, y, 64, 64)
    }
  }

  val tilesHigh = 11
  val tilesWide = 11
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
    println("painting!")
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

  /**
   * A spherical robot that bumps into walls to detect them
   */
  class Bumper() extends Robot {

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
      ("setLeftSpeed", Seq("Number"), setLeftSpeed _),
      ("setRightSpeed", Seq("Number"), setRightSpeed _),
      ("isCollisionDetected", Seq(), isCollisionDetected _),
      ("resetCollisionDetector", Seq(), resetCollisionDetector _),
      ("getX", Seq.empty, () => x),
      ("getY", Seq.empty, () => y),
      ("getAngle", Seq.empty, () => angle),
    )

    override def x: Double = MicroRat.simToPixels(body.position.x.asInstanceOf[Double])
    override def y: Double = MicroRat.simToPixels(body.position.y.asInstanceOf[Double])

    def setPosition(x:Double, y:Double):Unit = {
      MatterSim.Body.setPosition(body, MatterSim.Vector.create(x, y))
    }

    def angle: Double = body.angle.asInstanceOf[Double]

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
      MatterSim.Body.setPosition(body, MatterSim.Vector.create(0, 0))
      MatterSim.Body.setVelocity(body, MatterSim.Vector.create(0, 0))
      MatterSim.Body.rotate(body, -body.angle)
      MatterSim.Body.setAngularVelocity(body, 0)

      _leftSpeed = 0
      _rightSpeed = 0

      setPosition(ONE_TILE + ONE_TILE / 2, ONE_TILE + ONE_TILE / 2)
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

  // Add objects for any wall tiles
  for {
    (row, y) <- cells.iterator.zipWithIndex
    (cell, x) <- row.iterator.zipWithIndex
  } {
    cell match {
      case WallTile =>
        addStaticBlock(x * ONE_TILE + ONE_TILE/2, y * ONE_TILE + ONE_TILE/2, ONE_TILE, ONE_TILE)
      case _ => //skip
    }
  }

  // Add sides
  addStaticBlock(-tilesWide * ONE_TILE / 2, 0, tilesWide * ONE_TILE, tilesHigh * ONE_TILE)
  addStaticBlock(3 * tilesWide * ONE_TILE / 2, 0, tilesWide * ONE_TILE, tilesHigh * ONE_TILE)
  addStaticBlock(0, -tilesHigh * ONE_TILE / 2 , tilesWide * ONE_TILE, tilesHigh * ONE_TILE)
  addStaticBlock(0, 3 * tilesHigh * ONE_TILE / 2, tilesWide * ONE_TILE, tilesHigh * ONE_TILE)

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
      (line, y) <- s.linesIterator.zipWithIndex
      (char, x) <- line.zipWithIndex
    } {
      if (actions.contains(char)) {
        actions(char)(m, x, y)
      }
    }
  }



}
