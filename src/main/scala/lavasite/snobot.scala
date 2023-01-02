package lavasite

import com.wbillingsley.veautiful.html.*
import coderunner.LoggingPrefabCodable
import lavamaze.Maze

def snobot = <.div(
    markdown.Fixed(
        """|# Snobot and the Lava Maze
           |
           |The Lava Maze is a grid-based game with a 1980s feel to it.
           |Our hero, Snobot, must make his way to the teleport to exit the level while escaping the nefarious Blob Guards.
           |
           |For example:
           |
           |""".stripMargin),
    LoggingPrefabCodable(
        """while(canGoRight()) {
        |  right();
        |}
        |""".stripMargin,
        Maze("Hello world")(viewSize = 8 -> 3, mazeSize = 8 -> 3) { 
            _.loadFromString(
              s"""
               | ZS...G
               |""".stripMargin)
        }
    ),
    

)