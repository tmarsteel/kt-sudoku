package com.github.tmarsteel.ktsudoku

import com.github.tmarsteel.ktsudoku.sudoku.Sudoku
import com.github.tmarsteel.ktsudoku.sudoku.solution.Canvas

fun main(args: Array<String>) {
    println("Input a sudoku; type 0 or - for unset fields")
    val sudoku = stdin.readSudoku()

    val canvas = Canvas(sudoku)
    for (cell in canvas.unsetCells) {
        println("$cell: ${cell.possibleValues}")
    }

    println()
    println("Render string:")

    for (row in 0..8) {
        for (column in 0..8) {
            val cell = canvas[row, column]
            if (cell.value == Sudoku.FieldValue.UNSET) {
                for (value in Sudoku.FieldValue.values()) {
                    if (value in cell.possibleValues) {
                        print(value.number)
                        print("#")
                    }
                }
            }
            else {
                print(cell.value.number)
            }

            if (column < 8) {
                print(",")
            }
        }
        if (row < 8) {
            print(";")
        }
    }

    println()
    println()
    println("Cells with 1 option filled:")

    for (cell in canvas.unsetCells) {
        if (cell.possibleValues.hasExactlyOne) {
            cell.set(cell.possibleValues.first)
        }
    }

    println(canvas.toSudoku().toOneLinerString())
}