package coderunner

import com.wbillingsley.veautiful.html.VHtmlNode

import scala.scalajs.js

/**
 * Something that can be included in a JSCodable
 */
trait Codable extends VHtmlNode {

  // Puts the Codable back in its start state
  def reset():Unit

  def functions():Seq[Codable.Triple]

}


object Codable {

  type Triple = (String, Seq[String], js.Function)

}