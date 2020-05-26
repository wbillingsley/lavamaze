package lavasite.templates

import com.wbillingsley.veautiful.html.{<, VHtmlNode, ^}
import lavasite.{Common, Route, Router}

case class FrontPage(banner:VHtmlNode, topMessage:VHtmlNode, topics:Seq[(Route, Topic)]) {

  def layout = {
    Common.shell(
      <.div(^.cls := "front-page",
        <.div(^.cls := "course-banner", banner),
        <.div(^.cls := "top-message", topMessage),
        <.div(^.cls := "topic-container",
          for { (r, t) <- topics } yield t.block(Router.path(r))
        )
      )
    )
  }


}
