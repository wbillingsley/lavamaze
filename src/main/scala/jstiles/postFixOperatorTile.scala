package jstiles

import com.wbillingsley.scatter.jstiles.{JSBlank, JSBlock, JSExpr, JSIfElse}
import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileSpace, TileText, VBox}

class PostfixOperatorTile(tileSpace:TileSpace[JSExpr], operator:String, socketType:Option[String]=None, val returnType:String = "void") extends Tile(tileSpace) {

  val left = new Socket(this, acceptType = socketType)

  override val tileContent = {
    HBox(left, TileText(operator))
  }

  def toLanguage:JSExpr = JSPostfix(
    left.content.map(_.toLanguage) getOrElse JSBlank,
    operator
  )

}


case class JSPostfix(l:JSExpr, op:String) extends JSExpr {
  def toJS(indent:Int) = {
    s"${l.toJS(indent)}$op"
  }
}