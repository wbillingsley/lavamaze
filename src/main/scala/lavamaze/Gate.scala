package lavamaze

import org.scalajs.dom.CanvasRenderingContext2D


case class Gate(tx:Int, ty:Int, diamonds:Int) extends Fixture {

  val x = tx * oneTile
  val y = ty * oneTile

  private var t = 0;
  private var remaining:Int = diamonds
  private var alpha = 0.5
  private var remainingText = s"$remaining"


  def intersects(x1:Int, y1:Int, x2:Int, y2:Int):Boolean = {
    inside(x, y)(x1, y1, x2, y2) ||
      inside(x1, y1)(x, y, x + oneTile, y + oneTile) ||
      inside(x2, y2)(x, y, x + oneTile, y + oneTile)
  }

  def intersects(bounds:((Int, Int), (Int, Int))):Boolean = {
    val ((x1, y1), (x2, y2)) = bounds
    intersects(x1, y1, x2, y2)
  }

  /**
   * Paint this mob on the canvas
   *
   * @param layer
   * @param ctx
   */
  override def paintLayer(layer: Int, x1: Int, y1: Int, x2: Int, y2: Int, ctx: CanvasRenderingContext2D): Unit = {
    if (layer == 6 && alpha > 0 && intersects(x1, y1, x2, y2)) {
      ctx.save()
      ctx.fillStyle = s"rgba(255, 0, 0, $alpha)"
      ctx.translate(x - x1 + halfTile, y - y1 + halfTile)
      ctx.fillRect(-halfTile, -halfTile, oneTile, oneTile)
      ctx.fillStyle = s"rgba(255, 255, 255, $alpha)"
      ctx.textBaseline = "middle"
      ctx.textAlign = "center"
      ctx.font = "32px monospace"
      ctx.fillText(remainingText, 0, 0)
      ctx.rotate(Math.PI / 4)
      ctx.strokeStyle = ctx.fillStyle
      ctx.lineWidth = 2
      ctx.strokeRect(-22, -22, 44, 44)
      ctx.restore()
    }
  }

  def tick(maze: Maze): Unit = {
    t += 1
    remaining = diamonds - maze.snobot.diamonds
    remainingText = s"$remaining"
    alpha = if (remaining > 0) {
      (0.5 + ((t / 4) % 5) * 0.05)
    } else alpha - ((t / 4) % 5) * 0.05
  }

  /** Whether this mob will block another mob's movement into a space */
  override def blockMovement(from: (Int, Int), to: (Int, Int), by: Mob): Boolean = by match {
    case s:Snobot => s.diamonds < diamonds && (tx, ty).crossedBy(from, to)
    case b:Boulder => remaining > 0 && (tx, ty).crossedBy(from, to)
    case _ => (tx, ty) == to
  }
}