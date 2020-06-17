package com.wbillingsley.scatter.jstiles

import com.wbillingsley.scatter.{HBox, Socket, SocketList, Tile, TileComponent, TileSpace, TileText, VBox}

case class JSDoWhile(t:JSBlock, cond: JSExpr) extends JSExpr {
  def toJS(indent:Int) = {
    val i = "  " * indent

    s"""${i}do {
       |${t.toJS(indent + 1)}
       |${i}} while (${cond.toJS(indent)})""".stripMargin
  }
}

class DoWhileTile(tileSpace:TileSpace[JSExpr]) extends Tile(tileSpace) {

  val condition = new Socket(this, acceptType = Some("Boolean"))
  val block = new SocketList(this, acceptType = Some("void"))

  override def returnType: String = "void"

  override val tileContent = {
    VBox(
      TileText("do {"),
      HBox(TileText("  "), block),
      HBox(TileText("} while ("), condition, TileText(")")),
    )
  }

  def toLanguage:JSExpr = JSDoWhile(
    JSBlock(
      block.sockets.flatMap(_.content).map(_.toLanguage)
    ),
    condition.content.map(_.toLanguage) getOrElse JSBlank
  )

}