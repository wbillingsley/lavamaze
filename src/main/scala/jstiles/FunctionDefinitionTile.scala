package jstiles

import com.wbillingsley.scatter.jstiles.{JSBlock, JSExpr}
import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileForeignObject, TileSpace, TileText, VBox}


case class JSFunction(name:String, params:Seq[String], body:JSBlock) extends JSExpr {
  def toJS(indent:Int) = {
    val i = "  " * indent

    s"""${i}function $name (${params.mkString(",")}) {
       |${body.toJS(indent + 1)}
       |${i}}""".stripMargin
  }
}

class FunctionDefinitionTile(tileSpace:TileSpace[JSExpr], var name:String, var params:Seq[String]) extends Tile(tileSpace) {

  override def returnType: String = "void"

  val body = new SocketList(this, acceptType = Some("void"))


  override val tileContent = {
    VBox(
      HBox(
        TileText[JSExpr]("function " + name + "(" + params.mkString(",") + ") {")
      ),
      HBox(TileText("  "), body),
      TileText("}")
    )
  }

  override def toLanguage: JSExpr = JSFunction(name, params, JSBlock(body.sockets.flatMap(_.content).map(_.toLanguage)))

}