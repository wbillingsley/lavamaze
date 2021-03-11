package coderunner

import com.wbillingsley.scatter.{Tile, TileSpace}
import com.wbillingsley.scatter.jstiles._
import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, VHtmlNode, ^}
import jstiles.{InfixOperatorTile, PostfixOperatorTile, ReturnTile, StringInputTile}
import lavasite.templates.AceEditor
import org.scalajs.dom.{Element, Node}

import scala.util.{Failure, Random, Success}

case class JSCodable(codable: Codable, underCodable: Option[JSCodable => VHtmlNode] = None)(
  fontSize:Int = 14,
  codeCanvasWidth:Int = 640, codeCanvasHeight:Int = 480, codeDrawHeight:Int = 400,
  buttonDrawerWidth:Int = 250,
  codeControlsHeight:Int = 50,
  consoleHeight:Int = 200,
  codableHeight: Option[Int] = None,
  var tilesMode: Boolean = true,
  var asyncify:Boolean = true
) extends VHtmlComponent {

  private val tileCanvas = new TileSpace(Some("example"), JSLang)((codeCanvasWidth, codeCanvasHeight))
  private val pt = new ProgramTile(tileCanvas, <.div(""))
  private val dt = new DeleteTile(tileCanvas)
  tileCanvas.addTiles((2d, 2d) -> pt, (codeCanvasWidth - 100d , 2d) -> dt)

  private val canvasContainer = <.div(^.cls := "canvas-container", ^.attr("style") := "max-height: 100%; overflow: scroll;", tileCanvas)

  val console = new OnScreenHtmlConsole(consoleHeight)

  private val functions:Seq[Codable.Triple] =
    codable.functions() ++
    Seq(
      ("println", Seq("String"), (s:Any) => console.println(s.toString))
    )

  private val codeRunner = new WorkerCodeRunner(
    ((for { (n, _, f) <- functions } yield n -> f).toMap),
    Map.empty, asyncify)

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
    prependButtons = (if (tilesMode) Seq(textView, clear) else Seq(tileView, clear)),
    onComplete = {
      case Success(x) => console.printlnStyled("Completed with: " + x.toString, "success")
      case Failure(x) => console.printlnStyled(x.getMessage, "error")
    }
  )

  private def addTileToCanvas(t:Tile[JSExpr]) = {
    tileCanvas.addTiles((250d + Random.nextInt(10), 100d + Random.nextInt(10)) -> t)
  }

  private def addTileCode(t:Tile[JSExpr]):Unit = {
    if (tilesMode) {
      addTileToCanvas(t)
    } else {
      aceCanvas.insertAtCursor(t.toLanguage.toJS(0))
    }
  }

  def addCallTile(name:String, paramTypes:Seq[String]):Unit = {
    addTileCode(new FunctionCallTile(tileCanvas, name, paramTypes))
  }

  def addDeclareTile(name:String, paramTypes:Seq[String]):Unit = {
    addTileCode(new FunctionDefinitionTile(tileCanvas, name, paramTypes))
  }


  object FunctionForm extends VHtmlComponent {
    var name = ""
    var params = ""

    def declare() = {
      addDeclareTile(name, params.split(",").map(_.trim).filter(_.nonEmpty))
    }

    def call() = {
      addCallTile(name, params.split(",").map(_.trim).filter(_.nonEmpty))
    }

    override protected def render: DiffNode[Element, Node] = {
      import com.wbillingsley.veautiful.html.EventMethods

      <.div(
        <.div(^.cls := "input-group",
          <.div(^.cls := "input-group-prepend",
            <.span(^.cls := "input-group-text", "Name")
          ),
          <.input(^.cls := "form-control", ^.attr("type") := "text", ^.prop("value") := name, ^.prop("placeholder") := "myFunction",
            ^.on("input") ==> { e => name = e.inputValue.getOrElse(""); rerender() }
          )
        ),
        <.div(^.cls := "input-group",
          <.div(^.cls := "input-group-prepend",
            <.span(^.cls := "input-group-text", "Params")
          ),
          <.input(^.cls := "form-control", ^.attr("type") := "text", ^.prop("value") := params, ^.prop("placeholder") := "a, b, c",
            ^.on("input") ==> { e => params = e.inputValue.getOrElse(""); rerender() }
          )
        ),
        <.div(^.cls := "btn-group",
          <.button(^.cls := "btn btn-outline-primary", "Declare", ^.onClick --> declare(), ^.attr("disabled") ?= (if (name.trim.isEmpty) Some("disabled") else None)),
          <.button(^.cls := "btn btn-outline-primary", "Call", ^.onClick --> call(), ^.attr("disabled") ?= (if (name.trim.isEmpty) Some("disabled") else None)),
        )
      )
    }
  }

  object VariableForm extends VHtmlComponent {
    var name = ""

    def assign() = {
      addTileCode(new AssignmentTile(tileCanvas, name))
    }

    def let() = {
      addTileCode(new LetTile(tileCanvas, name))
    }

    def use() = {
      addTileCode(new VariableTile(tileCanvas, name))
    }

    override protected def render: DiffNode[Element, Node] = {
      import com.wbillingsley.veautiful.html.EventMethods

      <.div(
        <.div(^.cls := "input-group",
          <.div(^.cls := "input-group-prepend",
            <.span(^.cls := "input-group-text", "Name")
          ),
          <.input(^.cls := "form-control", ^.attr("type") := "text", ^.prop("value") := name, ^.prop("placeholder") := "myVar",
            ^.on("input") ==> { e => name = e.inputValue.getOrElse(""); rerender() }
          )
        ),
        <.div(^.cls := "btn-group",
          <.button(^.cls := "btn btn-outline-primary", "Declare", ^.onClick --> let(), ^.attr("disabled") ?= (if (name.trim.isEmpty) Some("disabled") else None)),
          <.button(^.cls := "btn btn-outline-primary", "Update", ^.onClick --> assign(), ^.attr("disabled") ?= (if (name.trim.isEmpty) Some("disabled") else None)),
          <.button(^.cls := "btn btn-outline-primary", "Use", ^.onClick --> use(), ^.attr("disabled") ?= (if (name.trim.isEmpty) Some("disabled") else None)),
        )
      )
    }
  }

  /** Tile buttons for if and loops */
  private val controlButtons = Seq(
    <.button(^.cls := "btn btn-outline-secondary", "if ... else ...", ^.onClick --> addTileCode(new IfElseTile(tileCanvas))),
    <.button(^.cls := "btn btn-outline-secondary", "do ... while ...", ^.onClick --> addTileCode(new DoWhileTile(tileCanvas))),
    <.button(^.cls := "btn btn-outline-secondary", "while ...", ^.onClick --> addTileCode(new WhileTile(tileCanvas))),
    <.button(^.cls := "btn btn-outline-secondary", "for ...", ^.onClick --> addTileCode(new ForTile(tileCanvas))),
  )

  /** Tile buttons for if and loops */
  private val mathButtons = Seq(
    <.button(^.cls := "btn btn-outline-secondary", "_++", ^.attr("title") := "increment", ^.onClick --> addTileCode(new PostfixOperatorTile(tileCanvas, "++", Some("number"), "number"))),
    <.button(^.cls := "btn btn-outline-secondary", "_ + _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "+"))),
    <.button(^.cls := "btn btn-outline-secondary", "_ - _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "-"))),
    <.button(^.cls := "btn btn-outline-secondary", "_ * _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "*"))),
    <.button(^.cls := "btn btn-outline-secondary", "_ / _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "/"))),
    <.button(^.cls := "btn btn-outline-secondary", "_ % _", ^.attr("title") := "modulo", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "%"))),
  )

  /** Tile buttons for if and loops */
  private val comparisonButtons = Seq(
    <.button(^.cls := "btn btn-outline-secondary", "_ == _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "=="))),
    <.button(^.cls := "btn btn-outline-secondary", "_ === _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "==="))),
    <.button(^.cls := "btn btn-outline-secondary", "_ < _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "<"))),
    <.button(^.cls := "btn btn-outline-secondary", "_ <= _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, "<="))),
    <.button(^.cls := "btn btn-outline-secondary", "_ > _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, ">"))),
    <.button(^.cls := "btn btn-outline-secondary", "_ >= _", ^.onClick --> addTileCode(new InfixOperatorTile(tileCanvas, ">="))),
  )


  private val inputs = Seq(
    <.button(^.cls := "btn btn-outline-secondary", "Number", ^.onClick --> addTileCode(new NumberInputTile(tileCanvas))),
    <.button(^.cls := "btn btn-outline-secondary", "String", ^.onClick --> addTileCode(new StringInputTile(tileCanvas))),
  )

  private val buttonDrawer = ButtonDrawer(
    "Arithmetic", mathButtons,
    "Comparisons", comparisonButtons,
    "Control", controlButtons,
    "Functions", FunctionForm, Seq(<.button(^.cls := "btn btn-outline-secondary", "return ...", ^.onClick --> addTileCode(new ReturnTile(tileCanvas)))),
    "Game",
    (for ((label, params, _) <- functions) yield <.button(^.cls := "btn btn-outline-secondary", label, ^.onClick --> addCallTile(label, params))),
    "Input Fields", inputs,
    "Variables", VariableForm,
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

  private def calcHeight:Int = codeDrawHeight + codeControlsHeight + consoleHeight

  override protected def render: DiffNode[Element, Node] = {
    <.div(^.cls := "jscodable", ^.attr("style") := s"display: grid; grid-template-columns: ${leftWidth}px auto; height: ${calcHeight}px",
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
      codable.vnode, underCodable.map(_.apply(this))
    )
  }

  override def afterAttach(): Unit = {
    super.afterAttach()
    tileCanvas.layout()
  }
}
