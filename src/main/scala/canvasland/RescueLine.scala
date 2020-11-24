package canvasland

import org.scalajs.dom

import scala.scalajs.js
import scala.util.Random

object RescueLine {
  type TileFunction = (Double, Double, Double, dom.CanvasRenderingContext2D) => Unit

  val tileSize = 120
  val halfTile:Double = tileSize / 2
  val quarterTile:Double = tileSize / 4.0
  val eighthTile:Double = tileSize / 4.0
  val lineWidth = 8
  val lineStyle = "rgb(60,60,60)"

  val startStyle = "rgb(0,180,60)"
  val endStyle = "rgb(180,0,60)"
  val rescueZoneStyle = "rgb(240,120,0)"
  val liveVictimStyle = "rgb(60,120,60)"

  val FACING_EAST:Double = 0
  val FACING_SOUTH:Double = Math.PI / 2
  val FACING_WEST:Double = Math.PI
  val FACING_NORTH:Double = - Math.PI / 2

  private def tileFunction(f:dom.CanvasRenderingContext2D => Unit) = {
    (tx:Double, ty:Double, angle:Double, ctx:dom.CanvasRenderingContext2D) =>
      ctx.save()
      ctx.lineWidth = lineWidth
      ctx.strokeStyle = lineStyle
      ctx.lineJoin = "round"
      ctx.lineCap = "round"
      ctx.translate(tx * tileSize + tileSize / 2, ty * tileSize + tileSize / 2)
      ctx.rotate(angle)

      f(ctx)
      ctx.restore()
  }

  val start:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(0, 0)
    ctx.lineTo(halfTile, 0)
    ctx.stroke()
    ctx.fillStyle = startStyle
    ctx.fillRect(-quarterTile, -quarterTile, quarterTile, halfTile)
  }

  val end:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(0, 0)
    ctx.lineTo(-halfTile, 0)
    ctx.stroke()
    ctx.fillStyle = endStyle
    ctx.fillRect(0, -quarterTile, quarterTile, halfTile)
  }

  val straight:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(halfTile, 0)
    ctx.stroke()
  }

  val dashed:TileFunction = tileFunction { ctx =>
    ctx.setLineDash(js.Array(8d, 16d))
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(halfTile, 0)
    ctx.stroke()
  }

  val crossRoad:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(halfTile, 0)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(0, -halfTile)
    ctx.lineTo(0, halfTile)
    ctx.stroke()
  }

  val sharpTurnLeft:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(0, 0)
    ctx.lineTo(0, -halfTile)
    ctx.stroke()
  }

  val rescueZone:TileFunction = tileFunction { ctx =>
    ctx.fillStyle = rescueZoneStyle
    ctx.fillRect(-halfTile, -halfTile, tileSize, tileSize)
  }

  val rescueSurvivor:TileFunction = tileFunction { ctx =>
    ctx.fillStyle = rescueZoneStyle
    ctx.fillRect(-halfTile, -halfTile, tileSize, tileSize)

    val x = Random.nextDouble * halfTile - quarterTile
    val y = Random.nextDouble * halfTile - quarterTile
    ctx.fillStyle = liveVictimStyle
    ctx.beginPath()
    ctx.arc(x, y, tileSize / 8, 0, Math.PI * 2)
    ctx.fill()
  }

  val sharpTurnRight:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(0, 0)
    ctx.lineTo(0, halfTile)
    ctx.stroke()
  }

  val zigzag:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(-(3.0/8) * tileSize, 0)
    ctx.lineTo(-(2.0/8) * tileSize, quarterTile)
    ctx.lineTo(-(1.0/8) * tileSize, 0)
    ctx.lineTo(0, -quarterTile)
    ctx.lineTo((1.0/8) * tileSize, 0)
    ctx.lineTo((2.0/8) * tileSize, quarterTile)
    ctx.lineTo((3.0/8) * tileSize, 0)
    ctx.lineTo(halfTile, 0)
    ctx.stroke()
  }

  val hump:TileFunction = tileFunction { ctx =>
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(-quarterTile, 0)
    ctx.stroke()
    ctx.beginPath()
    ctx.arc(0, 0, quarterTile, 0, Math.PI, true)
    ctx.stroke()
    ctx.beginPath()
    ctx.lineTo(quarterTile, 0)
    ctx.lineTo(halfTile, 0)
    ctx.stroke()
  }


  val dashedHump:TileFunction = tileFunction { ctx =>
    ctx.setLineDash(js.Array(24d, 18d))
    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(-quarterTile, 0)
    ctx.stroke()
    ctx.beginPath()
    ctx.arc(0, 0, quarterTile, 0, Math.PI)
    ctx.stroke()
    ctx.beginPath()
    ctx.lineTo(quarterTile, 0)
    ctx.lineTo(halfTile, 0)
    ctx.stroke()
  }

  val teeLeft:TileFunction = tileFunction { ctx =>
    ctx.fillStyle = startStyle
    val eighth = (1.0/8) * tileSize
    ctx.fillRect(-eighth, -eighth, eighth, eighth)

    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(0, 0)
    ctx.moveTo(0, -halfTile)
    ctx.lineTo(0, halfTile)
    ctx.stroke()
  }

  val teeRight:TileFunction = tileFunction { ctx =>
    ctx.fillStyle = startStyle
    val eighth = (1.0/8) * tileSize
    ctx.fillRect(-eighth, 0, eighth, eighth)

    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(0, 0)
    ctx.moveTo(0, -halfTile)
    ctx.lineTo(0, halfTile)
    ctx.stroke()
  }

  val roundaboutLeft:TileFunction = tileFunction { ctx =>
    ctx.fillStyle = startStyle
    val eighth = (1.0/8) * tileSize
    ctx.fillRect(-3 * eighth, -eighth, eighth, eighth)
    ctx.fillRect(-eighth, -3 * eighth, eighth, eighth)

    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(-quarterTile, 0)
    ctx.moveTo(0, -quarterTile)
    ctx.lineTo(0, -halfTile)
    ctx.stroke()
    ctx.beginPath()
    ctx.arc(0, 0, quarterTile, 0, 2 * Math.PI)
    ctx.stroke()
  }

  val roundaboutAhead:TileFunction = tileFunction { ctx =>
    ctx.fillStyle = startStyle
    val eighth = (1.0/8) * tileSize
    ctx.fillRect(-3 * eighth, -eighth, eighth, eighth)
    ctx.fillRect(quarterTile, -eighth, eighth, eighth)

    ctx.beginPath()
    ctx.moveTo(-halfTile, 0)
    ctx.lineTo(-quarterTile, 0)
    ctx.moveTo(halfTile, 0)
    ctx.lineTo(quarterTile, 0)
    ctx.stroke()
    ctx.beginPath()
    ctx.arc(0, 0, quarterTile, 0.75 * Math.PI, 2.25 * Math.PI)
    ctx.stroke()
  }

}
