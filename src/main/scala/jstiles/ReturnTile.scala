package jstiles

import com.wbillingsley.scatter.jstiles.{JSBlank, JSBlock, JSExpr, JSIfElse}
import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileSpace, TileText, VBox}

class ReturnTile(tileSpace:TileSpace[JSExpr], val returnType:String = "any") extends Tile(tileSpace) {

  val left = new Socket(this)

  override val tileContent = {
    HBox(TileText("return "), left)
  }

  def toLanguage:JSExpr = JSReturn(
    left.content.map(_.toLanguage) getOrElse JSBlank
  )

}


case class JSReturn(l:JSExpr) extends JSExpr {

  def toJS(indent:Int) = {
    val i = "  " * indent

    s"""${i}return ${l.toJS(indent)}"""
  }
}