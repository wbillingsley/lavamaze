package lavamaze

import java.util.NoSuchElementException

import coderunner.Codable
import com.wbillingsley.veautiful.html.{<, VHtmlElement, ^, DElement}
import org.scalajs.dom

import scala.scalajs.js.JSConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.js.JavaScriptException

case class CodableMaze(name:String = "maze")(
  viewSize:(Int, Int),
  mazeSize:(Int, Int),

  mazeSetup: Maze => _,

) {

  val maze = Maze(name)(viewSize, mazeSize)(mazeSetup)

  val dpad = NinePad(name)(
    tm = Some("⬆" -> (() => {})),
    ml = Some("⬅" -> (() => {})),
    mr = Some("➡" -> (() => {})),
    bm = Some("⬇" -> (() => {})),
  )



}


case class Maze(name:String = "maze")(
  viewSize:(Int, Int),
  mazeSize:(Int, Int),
)(setup: Maze => _) extends VHtmlElement with Codable {

  /** A function called each tick, that can be altered */
  var onTick: Maze => Unit = { _ => }

  def vnode = this

  val (mWidth, mHeight) = mazeSize
  val (vWidth, vHeight) = viewSize
  private val drawWidth = vWidth * oneTile
  private val drawHeight = vHeight * oneTile

  val environment = LavaEnvironment(mWidth, mHeight)

  private val canvas = <.mutable.canvas(^.attr("width") := drawWidth, ^.attr("height") := drawHeight)

  def domNode:Option[dom.html.Canvas] = canvas.domNode
  def attach() = canvas.attach()
  override def detach(): Unit = canvas.detach()

  private var started = false

  def start(): Unit = {
    started = true
  }

  var lastFrame:Double = 0
  override def afterAttach(): Unit = {
    dom.window.requestAnimationFrame(animationFrameHandler)
  }

  val animationFrameHandler: Double => Unit = (d:Double) => {
    val ticks = ((d - lastFrame) / tickPeriod).toInt
    for { tick <- 0 until ticks } if (started) step()
    if (ticks > 0) {
      lastFrame = d
      try {
        repaint()
      } catch {
        case x:Throwable => dom.console.error(x.getMessage)
      }
    }

    if (isAttached) {
      dom.window.requestAnimationFrame(animationFrameHandler)
    }
  }


  val snobot:Snobot = Snobot(this)
  var snobotStart:(Int, Int) = (0, 0)

  private val cells:Seq[Array[Tile]] = for { y <- 0 until mHeight} yield Array.fill[Tile](mWidth)(environment.defaultTile)
  private val fixtures:mutable.Map[(Int, Int), Fixture] = mutable.Map.empty
  private val mobs:mutable.Buffer[Mob] = mutable.Buffer(snobot)
  private val overlays:mutable.Set[Overlay] = mutable.Set.empty

  def allMobs = mobs.toSeq

  /** Gets the location of any goal fixtures */
  def getGoals:Seq[(Int, Int)] = fixtures.toSeq.collect { case ((x, y), Goal(tx, ty)) => (x, y) }

  def getGoalX:Int = getGoals.headOption.map(_._1).getOrElse(-1)
  def getGoalY:Int = getGoals.headOption.map(_._2).getOrElse(-1)

  def getTile(tx:Int, ty:Int):Tile = {
    if (tx >= mWidth || tx < 0 || ty < 0 || ty >= mHeight) Tile.OutOfBounds else cells(ty)(tx)
  }

  def getTile(t:(Int, Int)):Tile = getTile(t._1, t._2)

  def cellsIntersecting(box:((Int, Int), (Int, Int))):Seq[Tile] = {
    val ((x1, y1), (x2, y2)) = box
    for {
      x <- (x1 / oneTile) to (x2 / oneTile)
      y <- (y1 / oneTile) to (y2 / oneTile)
    } yield getTile(x, y)
  }

  def mobsIntersecting(box:((Int, Int), (Int, Int))):Set[Mob] = {
    mobs.filter(m => intersects(m.hitBox, box)).toSet
  }

  def mobsInTile(t:(Int, Int)):Set[Mob] = mobsIntersecting(t * oneTile, t * oneTile + (oneTile, oneTile))

  /**
   * Whether potential movement by a mob from one location to another should be stopped.
   */
  def blockMovement(from:(Int, Int), to:(Int, Int), by:Mob):Boolean = {
    getTile(to).isBlockingTo(by) ||
      Seq(from, to, from._1 -> to._2, to._1 -> from._2).exists(p => getTile(p).blockMovement(p)(from, to, by)) ||
      mobs.exists { x => x != by && x.blockMovement(from, to, by) } ||
      fixtures.values.exists(_.blockMovement(from, to, by))
  }

  def setTile(tx:Int, ty:Int, tile: Tile):Unit = {
    if (tx < mWidth && tx >= 0 && ty >= 0 && ty < mHeight) cells(ty)(tx) = tile
  }

  def addFixture(f:Fixture):Unit = {
    if (f.tx < mWidth && f.tx >= 0 && f.ty >= 0 && f.ty < mHeight) fixtures.addOne((f.tx, f.ty) -> f)
  }

  def removeFixture(f:Fixture):Unit = {
    fixtures.remove((f.tx, f.ty))
  }

  def addOverlay(o:Overlay):Unit = {
    overlays.add(o)
  }

  def getOverlays = overlays.toSet

  def addMob(m:Mob):Unit = {
    mobs.append(m)
  }


  def reset(): Unit = {
    for {
      row <- cells
      x <- row.indices
    } row(x) = environment.defaultTile

    started = false
    fixtures.clear()
    overlays.clear()
    mobs.clear()
    mobs.append(snobot)
    snobot.reset()
    setup(this)
    snobot.putAtTile(snobotStart)
    overlays.foreach(_.reset(this))
  }



  def loadFromString(s:String) = QuickMaze.process(this, s)

  def repaint():Unit = for (c <- domNode) {
    val ctx = c.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    for { layer <- 0 until 10 } {
      environment.paintLayer(layer, 0, 0, vWidth * oneTile, vHeight * oneTile, ctx)

      for {
        (row, y) <- cells.iterator.zipWithIndex
        (cell, x) <- row.iterator.zipWithIndex
      } {
        cell.paint(layer, x * oneTile, y * oneTile, ctx)

        ctx.save()
        ctx.translate(x * oneTile, y * oneTile)
        for { o <- overlays } o.paintTile(layer, x, y, ctx)
        ctx.restore()
      }

      for {
        fixture <- fixtures.values
      } fixture.paintLayer(layer, 0, 0, vWidth * oneTile, vHeight * oneTile, ctx)

      snobot.paintLayer(layer, 0, 0, vWidth, vHeight, ctx)

      for {
        mob <- mobs
      } {
        mob.paintLayer(layer, 0, 0, vWidth * oneTile, vHeight * oneTile, ctx)
      }
    }
  }


  def step() = {
    environment.tick()
    fixtures.values.foreach(_.tick(this))
    mobs.foreach(_.tick(this))
    overlays.foreach(_.tick(this))
    onTick(this)
  }

  /** User-defined additional functions */
  var additionalFunctions:Seq[Codable.Triple] = Seq.empty

  val snobotFunctions:Seq[Codable.Triple] = Seq(
    ("left", Seq.empty, () => snobot.ask(Snobot.MoveMessage(lavamaze.WEST)).toJSPromise),
    ("right", Seq.empty, () => snobot.ask(Snobot.MoveMessage(lavamaze.EAST)).toJSPromise),
    ("up", Seq.empty, () => snobot.ask(Snobot.MoveMessage(lavamaze.NORTH)).toJSPromise),
    ("down", Seq.empty, () => snobot.ask(Snobot.MoveMessage(lavamaze.SOUTH)).toJSPromise),
    ("canGoRight", Seq.empty, () => snobot.canMove(lavamaze.EAST)),
    ("canGoUp", Seq.empty, () => snobot.canMove(lavamaze.NORTH)),
    ("canGoDown", Seq.empty, () => snobot.canMove(lavamaze.SOUTH)),
    ("canGoLeft", Seq.empty, () => snobot.canMove(lavamaze.WEST)),
    ("uptime", Seq.empty, () => snobot.uptime().toInt)
  )


  /**
   * Finds dogbot if it is in the maze
   */
  def dogbot:Option[Dogbot] = mobs.toSeq.collectFirst { case d: Dogbot => d }

  def askDogbot(m: Dogbot.Message):Future[Unit] = dogbot match {
    case Some(d) => d.ask(m)
    case _ => Future.failed(JavaScriptException("Dogbot is not in the maze"))
  }

  val dogbotFunctions:Seq[Codable.Triple] = Seq(
    ("dogbotLeft", Seq.empty, () => askDogbot(Dogbot.MoveMessage(lavamaze.WEST)).toJSPromise),
    ("dogbotRight", Seq.empty, () => askDogbot(Dogbot.MoveMessage(lavamaze.EAST)).toJSPromise),
    ("dogbotUp", Seq.empty, () => askDogbot(Dogbot.MoveMessage(lavamaze.NORTH)).toJSPromise),
    ("dogbotDown", Seq.empty, () => askDogbot(Dogbot.MoveMessage(lavamaze.SOUTH)).toJSPromise),
  )

  /** The functions that should be exposed to the coding environment */
  def functions():Seq[Codable.Triple] = {
    snobotFunctions concat additionalFunctions
  }

  reset()

}

object Maze {




}
