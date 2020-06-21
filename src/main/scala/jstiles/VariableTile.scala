package jstiles

import com.wbillingsley.scatter.jstiles.{JSBlank, JSBlock, JSExpr}
import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileForeignObject, TileSpace, TileText, VBox}

case class JSVariable(name:String) extends JSExpr {
  def toJS(indent:Int) = {
    val i = "  " * indent
    s"${i}$name"
  }
}


class VariableTile(tileSpace:TileSpace[JSExpr], name:String) extends Tile(tileSpace) {

  override val tileContent = {
    HBox(
      TileText[JSExpr](name)
    )
  }

  override def toLanguage: JSExpr = JSVariable(name)

  override def returnType: String = "any"
}