package canvasland

import coderunner.Codable
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, DHtmlComponent, VHtmlElement, ^}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node, html}

import scala.collection.mutable
import scala.scalajs.js

case class CanvasLand(name:String = "canvasland")(
  viewSize:(Int, Int) = 640 -> 640,
  fieldSize:(Int, Int) = 640 -> 640,
  r:Robot,
  setup: CanvasLand => Unit
) extends Codable.CanvasLike with DHtmlComponent {

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

  override def vnode: VHtmlElement = this

  /** The field of play, which can be drawn to by the set-up */
  private val (fieldWidth, fieldHeight) = fieldSize
  private val fieldCanvas = <.canvas(^.attr("width") := fieldWidth, ^.attr("height") := fieldHeight).build().create()

  /** Renders the currently visible section of the field of play, and any additional mark-up  */
  private val (viewWidth, viewHeight) = viewSize
  val renderCanvas = <.canvas(^.attr("width") := viewWidth, ^.attr("height") := viewHeight).build()

  /** Used for overlaying debug information */
  //val overlay = <.div(^.attr("width") := viewWidth, ^.attr("height") := viewHeight, ^.attr("position") := "absolute")

  /** Repaints the canvas */
  override def repaint():Unit = {
    for {
      canvas <- renderCanvas.domNode
      ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    } {
      val w = canvas.width
      val h = canvas.height
      ctx.clearRect(0, 0, w, h)

      // Safari (as at 2021/11/18) won't let drawImage copy from outside the bounds of a source image (it produces a blank result).
      // So, we need to work out the bounds we are copying.
      val (offsetX, offsetY) = viewOffset
      val sx = Math.max(0, offsetX) // leftmost pixel of the source to copy
      val sy = Math.max(0, offsetY) // topmost pixel of the source to copy
      val sw = Math.min(w, fieldCanvas.width - sx) // width of image to copy
      val sh = Math.min(h, fieldCanvas.height - sy) // height of image to copy
      val dx = Math.max(0, -offsetX) // if we had to "crop" sx to 0, we need to adjust dx so the cropped image appears in the right place
      val dy = Math.max(0, -offsetY) // if we had to "crop" sy to 0, we need to adjust dx so the cropped image appears in the right place

      ctx.drawImage(fieldCanvas, sx, sy, sw, sh, dx, dy, sw, sh) // copy the cropped image

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
  override protected def render = {
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
      image.addEventListener("load", { (_:Any) => _drawImage() })
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
