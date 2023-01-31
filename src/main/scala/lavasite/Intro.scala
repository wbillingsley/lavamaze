package lavasite

import com.wbillingsley.veautiful.html.{<, ^}

def intro = <.div(
  markdown.Fixed(
   s"""
    |# Welcome to the Lava Maze
    |
    |This is a suite of programmable game environments, built using [Veautiful](http://www.wbillingsley.com/veautiful) and Scala.js. 
    |
    |If you're here to play, you might want to jump right into [the challenges](${site.router.path(site.ListPathRoute("challenges", "snobot-lava-maze", Nil))})
    |
    |
    |
    |If you're a coder looking to import the game environments into another site, they can be imported using jitpack:
    |
    |```
    |resolvers += "jitpack" at "https://jitpack.io"
    |
    |libraryDependencies += "com.github.wbillingsley" % "lavamaze" % "master-SNAPSHOT"
    |```
    |
    |""".stripMargin)
)
