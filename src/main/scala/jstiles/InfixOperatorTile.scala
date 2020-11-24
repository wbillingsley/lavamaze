package jstiles

import com.wbillingsley.scatter.jstiles.{JSBlank, JSBlock, JSExpr, JSIfElse}
import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileSpace, TileText, VBox}

class InfixOperatorTile(tileSpace:TileSpace[JSExpr], operator:String) extends Tile(tileSpace) {

  val left = new Socket(this, acceptType = None)
  val right = new Socket(this, acceptType = None)

  override def returnType: String = "void"

  override val tileContent = {
    HBox(TileText("("), left, TileText(operator), right, TileText(")"))
  }

  def toLanguage:JSExpr = JSInfix(
    left.content.map(_.toLanguage) getOrElse JSBlank,
    operator,
    right.content.map(_.toLanguage) getOrElse JSBlank,
  )

}

case class JSInfix(l:JSExpr, op:String, r: JSExpr) extends JSExpr {
  def toJS(indent:Int) = {
    s"(${l.toJS(indent)} $op ${r.toJS(indent)})"
  }
}