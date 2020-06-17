package com.wbillingsley.scatter.jstiles

import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileSpace, TileText, VBox}

case class JSWhile(cond: JSExpr, t:JSBlock) extends JSExpr {
  def toJS(indent:Int) = {
    val i = "  " * indent

    s"""${i}while (${cond.toJS(indent)}) {
       |${t.toJS(indent + 1)}
       |${i}}""".stripMargin
  }
}

class WhileTile(tileSpace:TileSpace[JSExpr]) extends Tile(tileSpace) {

  val condition = new Socket(this, acceptType = Some("Boolean"))
  val block = new SocketList(this, acceptType = Some("void"))

  override def returnType: String = "void"

  override val tileContent = {
    VBox(
      HBox(TileText("while ("), condition, TileText(") {")),
      HBox(TileText("  "), block),
      TileText("}")
    )
  }

  def toLanguage:JSExpr = JSWhile(
    condition.content.map(_.toLanguage) getOrElse JSBlank,
    JSBlock(
      block.sockets.flatMap(_.content).map(_.toLanguage)
    )
  )

}