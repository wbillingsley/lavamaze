package lavasite

import com.wbillingsley.veautiful.html.{<, ^}

def intro = <.div(
  markdown.Fixed(
    """
    |# Welcome to the Lava Maze
    |
    |This is a suite of programmable game environments, built using [Veautiful](http://www.wbillingsley.com/veautiful) and Scala.js
    |
    |At the moment, this website holds some test decks to test the libraries are working o.k. The libraries are designed to be imported into Veautiful (and Doctacular) websites
    |
    |They can be imported using jitpack:
    |
    |```
    |resolvers += "jitpack" at "https://jitpack.io"
    |
    |libraryDependencies += "com.github.wbillingsley" % "lavamaze" % "master-SNAPSHOT"
    |```
    |
    |Publishing to Maven Central will come soon.
    |
    |""".stripMargin)
)
