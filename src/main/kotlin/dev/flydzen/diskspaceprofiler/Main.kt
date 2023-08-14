package dev.flydzen.diskspaceprofiler

import dev.flydzen.diskspaceprofiler.ui.View

fun main(args: Array<String>) {
    println("Hello World!")
    val controller = Controller()
    val view = View(controller)
//    val path = Path("C:\\Users\\flydz\\Downloads")
//    val file = path.toFile()
//    println(file.listFiles()?.joinToString("\n") {  it.path.toString() })
}