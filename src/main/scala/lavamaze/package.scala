import com.wbillingsley.veautiful.html.{<, ^}

import scala.scalajs.js

package object lavamaze {

  type Coordinate = (Double, Double)

  type Direction = Int
  val NORTH:Direction = 3
  val SOUTH:Direction = 1
  val EAST:Direction = 0
  val WEST:Direction = 2

  def oppositeDirection(d:Direction):Direction = d match {
    case NORTH => SOUTH
    case SOUTH => NORTH
    case EAST => WEST
    case WEST => EAST
  }

  type Layer = Int
  val ENVIRONMENT:Layer = 1
  val ENV_EFFECT:Layer = 2
  val FLOOR:Layer = 3
  val FLOOR_EFFECT:Layer = 4
  val MOB_LOW:Layer = 5
  val MOB_HIGH:Layer = 6
  val SKY_LOW:Layer = 7
  val SKY_HIGH:Layer = 8

  val oneTile = 64
  val halfTile: Int = oneTile / 2
  val quarterTile: Int = oneTile / 4
  val eighthTile: Int = oneTile / 8

  val tickRate = 32
  val tickPeriod:Double = 1000.0 / tickRate

  def tileToPixel(tileCoord:Int):Int = tileCoord * oneTile

  /** Checks if a point is inside a rectangle */
  def inside(x:Int, y:Int)(x1:Int, y1:Int, x2:Int, y2:Int):Boolean = {
    (x1 <= x && x <= x2 && y1 <= y && y <= y2)
  }

  def inside(p1:(Int, Int), r:((Int, Int), (Int, Int))):Boolean = {
    val (x, y) = p1
    val ((x1, y1), (x2, y2)) = r
    (x1 <= x && x <= x2 && y1 <= y && y <= y2)
  }

  def intersects(r1:((Int, Int), (Int, Int)), r2:((Int, Int), (Int, Int))):Boolean = {
    inside(r1._1, r2) || inside(r1._2, r2) || inside(r2._1, r1) || inside(r2._2, r1)
  }

  implicit class VectorOps(val v:(Int, Int)) extends AnyVal {
    def *(i:Int):(Int, Int) = (v._1 * i, v._2 * i)

    def +(v2:(Int, Int)):(Int, Int) = (v._1 + v2._1, v._2 + v2._2)

    /**
     * Whether a single-step tile move crosses this (x, y)
     */
    def crossedBy(from:(Int, Int), to:(Int, Int)):Boolean = {
      v == from || v == to || (v._1 == from._1 && v._2 == to._2) || (v._1 == to._1 && v._2 == from._2)
    }

  }

  val settings:js.Dictionary[Any] = {
    if (js.typeOf(js.Dynamic.global.lavaSettings) == "undefined") js.Dictionary.empty else js.Dynamic.global.lavaSettings.asInstanceOf[js.Dictionary[Any]]
  }

  def imageBase:String = {
     settings.getOrElse("imageBase", "").asInstanceOf[String]
  }

  def loadImage(s:String) = {
    <.img(^.src := s"${imageBase}images/$s").build().create()
  }


}
