package lavasite.lavadeck

import com.wbillingsley.veautiful.html.*
import com.wbillingsley.veautiful.doctacular.*

case class VNodeStage(n: () => VHtmlContent) extends Challenge.Stage {

  override def completion: Challenge.Completion = Challenge.Open

  override def kind: String = "text"

  override protected def render = textColumn(
    n(),
  )

}

object VNodeStage {

  def twoColumn(title:String)(left: () => VHtmlContent, right: () => VHtmlContent):VNodeStage = {
    VNodeStage(
      () => <.div(
        <.h2(title),
        Challenge.split(left())(right()),
      )
    )
  }

  def card(n:VHtmlContent) = <.div(^.cls := "card",
    <.div(^.cls := "card-body", n)
  )

}