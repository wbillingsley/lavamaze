package coderunner

import com.wbillingsley.scatter.{Tile, TileSpace}
import com.wbillingsley.scatter.jstiles.{DoWhileTile, ForTile, FunctionCall2Tile, FunctionCallTile, IfElseTile, JSExpr, JSLang, ProgramTile, WhileTile}
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import jstiles.lavamaze.DeleteTile
import lavasite.templates.AceEditor
import org.scalajs.dom.{Element, Node}

import scala.util.Random

case class JSCodable()(codable: Codable, underCodable: Option[JSCodable => VHtmlNode] = None)(
  fontSize:Int = 14,
  codeCanvasWidth:Int = 640, codeCanvasHeight:Int = 480, codeDrawHeight:Int = 400,
  buttonDrawerWidth:Int = 150,
  codableHeight: Option[Int] = None
) extends VHtmlComponent {

  var tilesMode = true

  private val tileCanvas = new TileSpace(Some("example"), JSLang)((codeCanvasWidth, codeCanvasHeight))
  private val pt = new ProgramTile(tileCanvas, <.div(""))
  private val dt = new DeleteTile(tileCanvas)
  pt.x = 2
  pt.y = 2
  dt.x = codeCanvasWidth - 100
  dt.y = 2
  tileCanvas.tiles.appendAll(Seq(pt, dt))

  private val canvasContainer = <.div(^.cls := "canvas-container", ^.attr("style") := "max-height: 100%; overflow: scroll;", tileCanvas)

  val console = new OnScreenConsole()

  private val functions:Seq[Codable.Triple] =
    codable.functions() ++
    Seq(
      ("println", Seq("String"), (s:Any) => console.println(s.toString))
    )

  private val codeRunner = new WorkerCodeRunner(
    ((for { (n, _, f) <- functions } yield n -> f).toMap),
    Map.empty, true)

  val aceCanvas = AceEditor("mycode") { editor =>
    editor.setTheme("ace/theme/dawn")
    editor.setFontSize("24px")
    editor.setOption("hasCssTransforms", true)
    editor.session.setMode("ace/mode/javascript")
  }

  val codePlayControls = CodePlayControls(codeRunner)(
    if (tilesMode) pt.toLanguage.toJS(2) else aceCanvas.value,
    start = codable.start _,
    reset = codable.reset _,
    prependButtons = (if (tilesMode) Seq(textView, clear) else Seq(tileView, clear))
  )

  private def addTileToCanvas(t:Tile[JSExpr]) = {
    t.x = 250 + Random.nextInt(10)
    t.y = 100 + Random.nextInt(10)
    tileCanvas.tiles.append(t)
    tileCanvas.update()
    tileCanvas.layout()
  }



  private def addTileCode(t:Tile[JSExpr]):Unit = {
    if (tilesMode) {
      addTileToCanvas(t)
    } else {
      aceCanvas.insertAtCursor(t.toLanguage.toJS(0))
    }
  }

  def addCallTile(name:String, paramTypes:Seq[String]):Unit = {
    addTileCode(new FunctionCall2Tile(tileCanvas, name, paramTypes))
  }

  private val buttonDrawer = ButtonDrawer(
    "Control",
    Seq(
      <.button(^.cls := "btn btn-outline-secondary", "if ... else ...", ^.onClick --> addTileCode(new IfElseTile(tileCanvas))),
      <.button(^.cls := "btn btn-outline-secondary", "do ... while ...", ^.onClick --> addTileCode(new DoWhileTile(tileCanvas))),
      <.button(^.cls := "btn btn-outline-secondary", "while ...", ^.onClick --> addTileCode(new WhileTile(tileCanvas))),
      <.button(^.cls := "btn btn-outline-secondary", "for ...", ^.onClick --> addTileCode(new ForTile(tileCanvas))),
    ),
    "Game",
    (for ((label, params, f) <- functions) yield <.button(^.cls := "btn btn-outline-secondary", label, ^.onClick --> addCallTile(label, params)))

  )


  private val tileView = <.button(^.cls := "btn btn-primary", ^.key := "tileView", "Tiles",
    ^.attr("title") := "Tiles mode", ^.onClick --> { tilesMode = true; rerender() }
  )
  private val textView = <.button(^.cls := "btn btn-primary", ^.key := "textView", "Text",
    ^.attr("title") := "Text mode", ^.onClick --> { tilesMode = false; rerender() }
  )
  private val clear = <.button(^.cls := "btn btn-primary", ^.key := "clear",
    ^.attr("title") := "Clear console", <("i")(^.cls := "material-icons", "clear"), ^.onClick --> { console.clear() }
  )

  private def leftWidth = codeCanvasWidth + buttonDrawerWidth

  override protected def render: DiffNode[Element, Node] = {
    <.div(^.cls := "jscodable", ^.attr("style") := s"display: grid; grid-template-columns: ${leftWidth}px auto;",
      <.div(^.cls := "left-area", ^.attr("style") := s"display: grid; grid-template-rows: ${codeDrawHeight}px 50px auto;",
        <.div(^.cls := "code-editing", ^.attr("style") := s"display: grid; grid-template-columns: ${buttonDrawerWidth}px auto;",
          buttonDrawer,
          if (tilesMode) canvasContainer else aceCanvas,
        ),
        <.div(^.cls := "code-controls",
          codePlayControls,
        ),
        console
      ),
      codable, underCodable.map(_.apply(this))
    )
  }

  override def afterAttach(): Unit = {
    super.afterAttach()
    tileCanvas.layout()
  }
}
