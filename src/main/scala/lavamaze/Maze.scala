package lavamaze

import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlNode, ^}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node}

import scala.collection.mutable

case class Maze(name:String = "maze")(
  viewSize:(Int, Int),
  mazeSize:(Int, Int),
)(setup: Maze => _) extends VHtmlNode {

  val (mWidth, mHeight) = mazeSize
  val (vWidth, vHeight) = viewSize
  val drawWidth = vWidth * oneTile
  val drawHeight = vHeight * oneTile

  val environment = LavaEnvironment(mWidth, mHeight)

  val canvas = <.canvas(^.attr("width") := drawWidth, ^.attr("height") := drawHeight)

  def domNode = canvas.domNode
  def attach() = canvas.attach()
  override def detach(): Unit = canvas.detach()


  var lastFrame:Double = 0
  override def afterAttach(): Unit = {
    dom.window.requestAnimationFrame(animationFrameHandler)
  }

  def animationFrameHandler(d:Double):Unit = {
    val ticks = ((d - lastFrame) / tickPeriod).toInt
    for { tick <- 0 until ticks } step()
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


  private val cells:Seq[Array[Tile]] = for { y <- 0 until mHeight} yield Array.fill[Tile](mWidth)(environment.defaultTile)

  private val fixtures:mutable.Map[(Int, Int), Fixture] = mutable.Map.empty

  def getTile(tx:Int, ty:Int):Tile = {
    if (tx >= mWidth || tx < 0 || ty < 0 || ty >= mHeight) Tile.OutOfBounds else cells(ty)(tx)
  }

  def setTile(tx:Int, ty:Int, tile: Tile):Unit = {
    if (tx < mWidth && tx >= 0 && ty >= 0 && ty < mHeight) cells(ty)(tx) = tile
  }

  def addFixture(f:Fixture):Unit = {
    if (f.tx < mWidth && f.tx >= 0 && f.ty >= 0 && f.ty < mHeight) fixtures.addOne((f.tx, f.ty) -> f)
  }

  val snobot = Snobot(this)
  setup(this)

  def reset(): Unit = {
    for {
      row <- cells
      x <- row.indices
    } row(x) = environment.defaultTile

    snobot.px = 0
    snobot.py = 0
    snobot.action = snobot.Idle()
    setup(this)
  }

  def repaint():Unit = for (c <- domNode) {
    val ctx = c.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    for { layer <- 0 until 10 } {
      environment.paintLayer(layer, 0, 0, vWidth * oneTile, vHeight * oneTile, ctx)

      for {
        (row, y) <- cells.iterator.zipWithIndex
        (cell, x) <- row.iterator.zipWithIndex
      } cell.paint(layer, x * oneTile, y * oneTile, ctx)

      for {
        fixture <- fixtures.values
      } fixture.paintLayer(layer, 0, 0, vWidth * oneTile, vHeight * oneTile, ctx)

      snobot.paintLayer(layer, 0, 0, vWidth, vHeight, ctx)
    }
  }


  def step() = {
    environment.tick()

    fixtures.values.foreach(_.tick(this))

    snobot.tick()
  }


}

object Maze {




}
