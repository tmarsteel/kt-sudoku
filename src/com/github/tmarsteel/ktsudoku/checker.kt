package com.github.tmarsteel.ktsudoku

fun main(args: Array<String>) {
    println("Input a sudoku; type 0 or - for unset fields")
    val sudoku = stdin.readSudoku()

    println("Contains contradictions? -> ${sudoku.containsContradictions}")
}