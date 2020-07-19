package canvasland

import coderunner.Codable
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node, html}

import scala.scalajs.js

case class CanvasLand(name:String = "canvasland")(
  viewSize:(Int, Int) = 640 -> 640,
  fieldSize:(Int, Int) = 640 -> 640,
  val r:Robot,
  setup: CanvasLand => Unit
) extends Codable.CanvasLike with VHtmlComponent {

  override def reset(): Unit = {
    stop()
    val ctx = fieldCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.clearRect(0, 0, fieldWidth, fieldHeight)
    setup(this)
    r.reset()
    repaint()
  }

  override def functions(): Seq[(String, Seq[String], js.Function)] = {
    r.functions() :+ ("setTickRate", Seq("Int"), (x:Int) => tickRate = x)
  }

  override def vnode: VHtmlNode = this

  /** The field of play, which can be drawn to by the set-up */
  private val (fieldWidth, fieldHeight) = fieldSize
  private val fieldCanvas = <.canvas(^.attr("width") := fieldWidth, ^.attr("height") := fieldHeight).create()

  /** Renders the currently visible section of the field of play, and any additional mark-up  */
  private val (viewWidth, viewHeight) = viewSize
  private val renderCanvas = <.canvas(^.attr("width") := viewWidth, ^.attr("height") := viewHeight)

  var x = 0
  var y = 0

  /** Repaints the canvas */
  override def repaint():Unit = {
    for {
      canvas <- renderCanvas.domNode
      ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    } {
      val w = canvas.width
      val h = canvas.height
      ctx.clearRect(0, 0, w, h)

      ctx.drawImage(fieldCanvas, x, y, w, h, 0, 0, w, h)

      ctx.save()
      ctx.translate(r.x - x, r.y - y)
      r.draw(ctx)
      ctx.restore()
    }
  }

  /** Render the component into the page */
  override protected def render: DiffNode[Element, Node] = {
    <.div(^.cls := "canvasland", ^.attr("width") := s"${viewWidth}px", ^.attr("height") := s"${viewHeight}px",
      renderCanvas,
    )
  }

  var tickRate = 128
  override def tickPeriod:Double = 1000.0 / tickRate

  override def step(): Unit = {
    r.step(this)
  }

  def drawGrid(style:String, spacing:Int, thickness:Int = 1):Unit = {
    val ctx = fieldCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.save()
    ctx.strokeStyle = style
    ctx.lineWidth = thickness
    ctx.beginPath()

    for {
      x <- (0 to fieldWidth).by(spacing)
      y <- (0 to fieldHeight).by(spacing)
    } {
      ctx.moveTo(0, y)
      ctx.lineTo(fieldWidth, y)
      ctx.moveTo(x, 0)
      ctx.lineTo(x, fieldHeight)
    }

    ctx.stroke()
    ctx.restore()
  }

  def fillCanvas(color:String):Unit = {
    val ctx = fieldCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.save()
    ctx.fillStyle = color
    ctx.fillRect(0, 0, fieldSize._1, fieldSize._2)
  }

  /** Runs arbitraty code against the field canvas's rendering context */
  def withCanvasContext(f:dom.CanvasRenderingContext2D => Unit):Unit = {
    val ctx = fieldCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.save()
    f(ctx)
    ctx.restore()
  }

  /** Marks a filled circle on the canvas, such as that left by the pen of the turtle */
  def fillCircle(p:Vec2, r:Double, fillStyle:String):Unit = withCanvasContext { ctx =>
    ctx.fillStyle = fillStyle
    ctx.beginPath()
    ctx.arc(p.x, p.y, r, 0, 2 * Math.PI)
    ctx.fill()
  }


  reset()
}
