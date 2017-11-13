package com.github.tmarsteel.ktsudoku

import com.github.tmarsteel.ktsudoku.sudoku.solution.Canvas

fun main(args: Array<String>) {
    println("Input a sudoku; type 0 or - for unset fields")
    val sudoku = stdin.readSudoku()

    if (sudoku.containsContradictions) {
        println("That input contains contradictions.")
        return
    }

    val canvas = Canvas(sudoku)

    var nSet = 0

    while (canvas.containsCellsWithOnlyOneOption) {
        for (cell in canvas.unsetCells) {
            cell.recalculatePossibleValues()
            if (cell.possibleValues.hasExactlyOne) {
                cell.set(cell.possibleValues.first)
                nSet++
            }
        }
    }

    val sudokuAfterDeductions = canvas.toSudoku()

    stdout.println()
    stdout.println()

    stdout.println("Was able to deduce $nSet cells. Here is what i found:")
    stdout.println()

    stdout.printSudoku(sudokuAfterDeductions)
    stdout.println()
    stdout.printSudokuAsOneLiner(sudokuAfterDeductions)
}

val Canvas.containsCellsWithOnlyOneOption: Boolean
    get() {
        for (cell in unsetCells) {
            cell.recalculatePossibleValues()
            if (cell.possibleValues.hasExactlyOne) {
                return true
            }
        }

        return false
    }