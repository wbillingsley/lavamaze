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


}
