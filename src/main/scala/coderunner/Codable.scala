package coderunner

import com.wbillingsley.veautiful.html.VHtmlNode

import scala.scalajs.js

/**
 * Something that can be included in a JSCodable
 */
trait Codable {

  // Puts the Codable back in its start state
  def reset():Unit

  // Starts the Codable ticking
  def start():Unit

  def functions():Seq[Codable.Triple]

  def vnode:VHtmlNode

}


object Codable {

  type Triple = (String, Seq[String], js.Function)

}