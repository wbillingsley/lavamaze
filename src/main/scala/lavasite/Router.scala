package lavasite

import com.wbillingsley.veautiful.PathDSL
import PathDSL.Extract._
import com.wbillingsley.veautiful.html.{<, VHtmlNode, ^}
import com.wbillingsley.veautiful.templates.{HistoryRouter, VSlides}

import scala.collection.mutable

sealed trait Route {
  def path:String
  def render: VHtmlNode
}
object Route {
  val introPath = /# / "home"
  val deckPath = /# / "deck" / stringParam / stringParam
}

case object IntroRoute extends Route {
  val path = Route.introPath.mkString(start)
  def render = Intro.frontPage.layout
}
case class DeckRoute(name:String, page:Int) extends Route {
  val path = Route.deckPath.mkString((page.toString, (name, start)))
  def render = Common.showDeck(name, page)
}

object Router extends HistoryRouter[Route] {

  var route:Route = IntroRoute

  def rerender() = renderElements(render())

  def render() = route.render

  override def path(route: Route): String = route.path

  def parseInt(s:String, or:Int):Int = {
    try {
      s.toInt
    } catch {
      case n:NumberFormatException => or
    }
  }
  override def routeFromLocation(): Route = PathDSL.hashPathList() match {
    case Route.introPath(start) => IntroRoute
    case Route.deckPath(((page, (name, _)), _))  => {
      DeckRoute(name, page.toInt)
    }
    case x =>
      IntroRoute
  }

}
