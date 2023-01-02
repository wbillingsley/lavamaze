package lavamaze

import com.wbillingsley.veautiful.DiffNode
import com.wbillingsley.veautiful.html.{<, VHtmlComponent, ^}
import org.scalajs.dom.{Element, Node}

/**
 * Nine buttons, arranged in a grid. Useful for D-Pads, etc.
 */
case class NinePad(name:String = "pad")(
  tl: Option[(String, () => Unit)] = None,
  tm: Option[(String, () => Unit)] = None,
  tr: Option[(String, () => Unit)] = None,
  ml: Option[(String, () => Unit)] = None,
  mm: Option[(String, () => Unit)] = None,
  mr: Option[(String, () => Unit)] = None,
  bl: Option[(String, () => Unit)] = None,
  bm: Option[(String, () => Unit)] = None,
  br: Option[(String, () => Unit)] = None
) extends VHtmlComponent {

  def button(o:Option[(String, () => Unit)]) = o match {
    case Some((label, f)) => <.button(^.cls := "btn", label, ^.onClick --> f())
    case _ => <.span()
  }

  override protected def render = {
    <.div(^.cls := "ninepad",
      button(tl), button(tm), button(tr),
      button(ml), button(mm), button(mr),
      button(bl), button(bm), button(br),
    )
  }

}

object NinePad {



}
