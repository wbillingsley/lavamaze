package lavasite.templates

import com.wbillingsley.veautiful.html.{<, VHtmlContent, ^}

case class Topic(name:String, image:VHtmlContent, content:VHtmlContent, cssClass:String = "", completion: () => String) {

  def block(path:String) = <.div(^.cls := s"topic-block $cssClass",
    <.a(^.href := path,
      <.div(^.cls := s"topic-image $cssClass", image),
      <.div(^.cls := "topic-label", <("label")(name)),
      <.div(^.cls := "topic-completion", completion())
    )
  )

}