package jstiles


import com.wbillingsley.scatter.jstiles.{JSExpr, JSNumber, JSString}
import com.wbillingsley.scatter.{HBox, Tile, TileComponent, TileForeignObject, TileSpace, TileText}
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, ^}
import com.wbillingsley.veautiful.logging.Logger
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement

class StringInputTile (tileSpace:TileSpace[JSExpr], width:Int = 10, initial:Option[String] = None) extends Tile(tileSpace) with VHtmlComponent {

  import NumberInputTile._

  var string:Option[String] = initial

  val input = <.input(^.attr("type") := "text", ^.cls := "scatter-text-input", ^.on("input") ==> onInput)

  override def afterAttach(): Unit = {
    super.afterAttach()
  }

  def onInput(e:dom.Event):Unit = {
    import com.wbillingsley.veautiful.html.EventMethods
    string = e.inputValue
    input.makeItSo(<.input(^.attr("type") := "text", ^.cls := "scatter-text-input", ^.on("input") ==> onInput, ^.prop("value") ?= string))
    rerender()
  }

  override val tileContent: TileComponent[JSExpr] = HBox(
    TileText("\""), HBox(TileForeignObject(input)), TileText("\"")
  )

  override def returnType: String = "String"

  override def toLanguage: JSExpr = JSString(string.getOrElse(""))
}

object NumberInputTile {
  val logger:Logger = Logger.getLogger(NumberInputTile.getClass)
}