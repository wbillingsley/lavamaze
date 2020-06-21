package jstiles

import com.wbillingsley.scatter.jstiles.{JSBlank, JSBlock, JSExpr}
import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileForeignObject, TileSpace, TileText, VBox}

case class JSLet(name:String, t:JSExpr) extends JSExpr {
  def toJS(indent:Int) = {
    val i = "  " * indent

    s"${i}let $name = ${t.toJS(0)}".stripMargin
  }
}


class LetTile(tileSpace:TileSpace[JSExpr], name:String) extends Tile(tileSpace) {

  override def returnType: String = "void"

  val condition = new Socket(this)

  override val tileContent = {
    HBox(
      TileText[JSExpr](s"let $name = "), condition
    )
  }

  override def toLanguage: JSExpr = JSLet(name, condition.content.map(_.toLanguage) getOrElse JSBlank)

}