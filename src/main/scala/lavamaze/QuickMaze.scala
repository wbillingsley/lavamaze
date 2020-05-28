package lavamaze

import scala.collection.mutable

object QuickMaze {

  private val actions:mutable.Map[Char, (Maze, Int, Int) => Unit] = mutable.Map(
    ' ' -> { case (m, x, y) => m.setTile(x, y, m.environment.defaultTile) },
    '.' -> { case (m, x, y) => m.setTile(x, y, FloorTile) },
    'S' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.snobotStart = (x, y)
    },
    'O' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new Boulder(x, y))
    },
    '*' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Diamond(x, y))
    },
    'G' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Goal(x, y))
    },
    'B' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new BlobGuard(x, y))
    },
  )

  def process(m:Maze, s:String):Unit = {
    for {
      (line, y) <- s.linesIterator.zipWithIndex
      (char, x) <- line.zipWithIndex if actions.contains(char)
    } {
      actions(char)(m, x, y)
    }
  }




}
