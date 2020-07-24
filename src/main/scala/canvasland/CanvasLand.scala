package canvasland

import coderunner.Codable
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node, html}

import scala.collection.mutable
import scala.scalajs.js

case class CanvasLand(name:String = "canvasland")(
  viewSize:(Int, Int) = 640 -> 640,
  fieldSize:(Int, Int) = 640 -> 640,
  r:Robot,
  setup: CanvasLand => Unit
) extends Codable.CanvasLike with VHtmlComponent {

  private val robot = r

  /** Additional items that should be stepped in a tick. For instance, if some items are driven off an external physics
   *  simulation, we'd want to step the simulation but draw the items. */
  private val steppables = mutable.Buffer[Steppable](r)

  override def reset(): Unit = {
    stop()
    val ctx = fieldCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.clearRect(0, 0, fieldWidth, fieldHeight)

    steppables.clear()
    steppables.append(r)
    robot.reset()

    setup(this)
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


  /** Repaints the canvas */
  override def repaint():Unit = {
    for {
      canvas <- renderCanvas.domNode
      ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    } {
      val w = canvas.width
      val h = canvas.height
      ctx.clearRect(0, 0, w, h)

      ctx.drawImage(fieldCanvas, viewOffset._1, viewOffset._2, w, h, 0, 0, w, h)

      // Some of the Steppable items are drawable. Draw them.
      steppables.foreach {
        case m:Mob =>
          ctx.save()
          ctx.translate(m.x - viewOffset._1, m.y- viewOffset._2)
          m.draw(ctx)
          ctx.restore()

        case _ => // skip
      }
    }
  }

  private var viewOffset = (0d, 0d)
  private var dragStart:Option[((Double, Double), (Double, Double))] = None

  def onMouseDown(evt:dom.Event):Unit = evt match { case e:dom.MouseEvent =>
    println("woohoo")
    dragStart = Some((viewOffset, (e.clientX, e.clientY)))
  }

  def onMouseMove(evt:dom.Event):Unit = evt match { case e:dom.MouseEvent =>
    dragStart match {
      case Some(((x0, y0), (mx0, my0))) if (e.buttons & 1) != 0 =>
        val dx = e.clientX - mx0
        val dy = e.clientY - my0

        viewOffset = (x0 - dx) -> (y0 - dy)
        repaint()
      case _ => // do nothing
    }
  }

  /** Render the component into the page */
  override protected def render: DiffNode[Element, Node] = {
    <.div(^.cls := "canvasland", ^.attr("width") := s"${viewWidth}px", ^.attr("height") := s"${viewHeight}px",
      ^.on("mousedown") ==> onMouseDown, ^.on("mousemove") ==> onMouseMove,
      renderCanvas,
    )
  }

  var tickRate = 128
  override def tickPeriod:Double = 1000.0 / tickRate

  override def step(): Unit = {
    steppables.foreach(_.step(this))
  }

  /** Adds an item on which step should be called at each tick. */
  def addSteppable(s:Steppable):Unit = {
    steppables.append(s)
  }

  /** Draws a graph-paper like grid on the field canvas */
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

  /** Draws an image to the field canvas. This is a little more complex because we must be sure the image has loaded first. */
  def drawImage(image:html.Image, offsetX:Int=0, offsetY:Int=0, width:Double, height:Double, canvasOffsetX:Int=0, canvasOffsetY:Int=0, canvasImageWidth:Double, canvasImageHeight:Double):Unit = {
    def _drawImage():Unit = withCanvasContext(_.drawImage(image, offsetX, offsetY, width, height, canvasOffsetX, canvasOffsetY, canvasImageWidth, canvasImageHeight))

    if (image.complete) {
      _drawImage()
    } else {
      image.addEventListener("load", { _:Any => _drawImage() })
    }
  }

  /** Fills the background of the canvas in a specified CSS colour. Important, because the canvas starts transparent. */
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
  def fillCircle(p:Vec2, r:Double, fillStyle:String, compositeOp:Option[String] = None):Unit = withCanvasContext { ctx =>
    compositeOp.foreach(op => { ctx.globalCompositeOperation = op })
    ctx.fillStyle = fillStyle
    ctx.beginPath()
    ctx.arc(p.x, p.y, r, 0, 2 * Math.PI)
    ctx.fill()
  }


  reset()
}
