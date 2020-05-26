package object lavamaze {

  type Coordinate = (Double, Double)

  type Direction = Int
  val NORTH:Direction = 3
  val SOUTH:Direction = 1
  val EAST:Direction = 0
  val WEST:Direction = 2

  val oneTile = 64
  val halfTile: Int = oneTile / 2
  val quarterTile: Int = oneTile / 4
  val eighthTile: Int = oneTile / 8

  val tickRate = 32
  val tickPeriod:Double = 1000.0 / tickRate

  def tileToPixel(tileCoord:Int):Int = tileCoord * oneTile

}
