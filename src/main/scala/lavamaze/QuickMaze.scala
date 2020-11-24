package lavamaze

import com.wbillingsley.veautiful.logging.Logger

import scala.collection.mutable

object QuickMaze {
  private val logger = Logger.getLogger(this.getClass)

  private val actions:mutable.Map[Char, (Maze, Int, Int) => Unit] = mutable.Map(
    ' ' -> { case (m, x, y) => m.setTile(x, y, m.environment.defaultTile) },
    '.' -> { case (m, x, y) => m.setTile(x, y, FloorTile) },
    '#' -> { case (m, x, y) => m.setTile(x, y, WallTile) },
    'S' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.snobotStart = (x, y)
    },
    'O' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new Boulder(m, x, y))
    },
    '<' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new Boulder(m, x, y, Some(WEST)))
    },
    '>' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new Boulder(m, x, y, Some(EAST)))
    },
    '^' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new Boulder(m, x, y, Some(NORTH)))
    },
    'v' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new Boulder(m, x, y, Some(SOUTH)))
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
      m.addMob(new BlobGuard(m, x, y)(BlobGuard.patrolAI))
    },
    'Z' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new BlobGuard(m, x, y)(BlobGuard.zeroInAI))
    },
    'd' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addMob(new Dogbot(m, x, y))
    },
    '1' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 1))
    },
    '2' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 2))
    },
    '3' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 3))
    },
    '4' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 4))
    },
    '5' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 5))
    },
    '6' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 6))
    },
    '7' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 7))
    },
    '8' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 8))
    },
    '9' -> { case (m, x, y) =>
      m.setTile(x, y, FloorTile)
      m.addFixture(new Gate(x, y, 9))
    },

  )

  def process(m:Maze, s:String):Unit = {
    for {
      (line, y) <- s.linesIterator.zipWithIndex
      (char, x) <- line.zipWithIndex
    } {
      if (actions.contains(char)) {
        actions(char)(m, x, y)
      } else {
        logger.error(s"Unknown quickmaze character: $char")
      }

    }
  }




}
