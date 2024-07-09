package lavasite.lavadeck

import com.wbillingsley.veautiful.html.*
import com.wbillingsley.veautiful.doctacular.Challenge
import Challenge.{Completion, Level}

import canvasland.{CanvasLand, Turtle}
import coderunner.{JSCodable, PrefabCodable, LoggingPrefabCodable}
import lavamaze.{FloorTile, Goal, Maze, Overlay}
import org.scalajs.dom.{Element, Node}

import lavasite.{markdown, styleSuite}
import scala.util.Random
import lavasite.lavadeck.StageCardsOfDoom.CardsOfDoom
import com.wbillingsley.veautiful.Keep
import lavamaze.BlobGuard.patrolAI


class LifeGame(name:String, width:Int, height:Int, cellSize:Int = 12, cellGap:Int = 1)(initial:String = "") extends DHtmlComponent with Keep((name, width, height)) {

    val board = stateVariable[Map[(Int, Int), Boolean]](parse(initial))

    val playing = stateVariable(false)

    val viewBox = s"0 0 ${width * (cellSize + cellGap)} ${height * (cellSize + cellGap)}"

    var lastTick = 0d

    val tickHandler: Double => Unit = (d:Double) => {
        // println(s"d = $d, last = $lastTick")
        if playing.value then
            org.scalajs.dom.window.requestAnimationFrame(tickHandler) 
            if (d - lastTick > 250) then 
                lastTick = d
                step()
                
    }

    def start() = 
        org.scalajs.dom.window.requestAnimationFrame(tickHandler)
        playing.value = true

    def stop() = 
        playing.value = false

    def liveNeighbours(x:Int, y:Int):Int = 
        Seq(
            (x-1, y-1), (x, y-1), (x+1, y-1),
            (x-1, y),              (x+1, y),
            (x-1, y+1), (x, y+1), (x+1, y+1),
        ).count(loc => board.value.getOrElse(loc, false))

    def toggle(x:Int, y:Int) = 
        board.value = board.value.updated((x, y), !board.value.getOrElse((x, y), false))

    def step() = 
        board.value = (
            for 
                y <- 0 until height 
                x <- 0 until width
                ln = liveNeighbours(x, y) 
            yield 
                if board.value.getOrElse((x, y), false) then
                    (x, y) -> (ln == 2 || ln == 3)
                else (x, y) -> (ln == 3)             
        ).toMap

    def clear() = 
        board.value = parse(initial)
        if playing.value then stop()

    def parse(s:String) = 
        val lines = s.linesIterator.toSeq
        (
            for 
                y <- lines.indices 
                x <- lines(y).indices
            yield 
                (x, y) -> (lines(y)(x) == '#')             
        ).toMap


    override def render = <.mutable.div(
        <.svg(^.attr.viewBox := viewBox,
            for 
                y <- 0 until height 
                x <- 0 until width 
            yield 
                if board.value.getOrElse((x, y), false) then
                    SVG.rect(
                        ^.attr.x := (x * (cellSize + cellGap)),
                        ^.attr.y := (y * (cellSize + cellGap)),
                        ^.attr.width := cellSize, ^.attr.height := cellSize,
                        ^.attr.rx := 1,
                        ^.attr.style := "fill: #8c2;",
                        ^.on.click --> toggle(x, y)
                    )
                else 
                    SVG.rect(
                        ^.attr.x := (x * (cellSize + cellGap)),
                        ^.attr.y := (y * (cellSize + cellGap)),
                        ^.attr.width := cellSize, ^.attr.height := cellSize,
                        ^.attr.rx := 1,
                        ^.attr.style := "fill: #444;",
                        ^.on.click --> toggle(x, y)
                    )
        ),
        
        if playing.value then <.div(
            <.button(^.cls := "btn btn-outline-secondary", ^.on.click --> stop(), <.i(^.cls := "material-symbols-outlined", "stop")),
            <.button(^.cls := "btn btn-outline-secondary", ^.on.click --> clear(), <.i(^.cls := "material-symbols-outlined", "clear")),
        ) else <.div( 
            <.button(^.cls := "btn btn-outline-secondary", ^.on.click --> start(), <.i(^.cls := "material-symbols-outlined", "play_arrow")),
            <.button(^.cls := "btn btn-outline-secondary", ^.on.click --> step(), <.i(^.cls := "material-symbols-outlined", "step")),
            <.button(^.cls := "btn btn-outline-secondary", ^.on.click --> clear(), <.i(^.cls := "material-symbols-outlined", "clear")),
        )
    
    )



    
}