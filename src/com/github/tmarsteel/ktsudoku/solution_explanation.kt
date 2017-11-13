package com.github.tmarsteel.ktsudoku

import com.github.tmarsteel.ktsudoku.sudoku.Sudoku
import com.github.tmarsteel.ktsudoku.sudoku.solution.Canvas
import com.github.tmarsteel.ktsudoku.sudoku.solution.Solver
import java.io.PrintStream

fun main(args: Array<String>) {
    println("Input a sudoku; type 0 or - for unset fields")
    val sudoku = stdin.readSudoku()

    solveAndExplain(sudoku, stdout)
}

fun solveAndExplain(sudoku: Sudoku, out: PrintStream, indentLevel: Int = 0) {
    fun explain(msg: String) {
        out.println(" ".repeat(indentLevel * 2) + msg)
    }

    explain("O " + sudoku.toOneLinerString())

    if (sudoku.containsContradictions) {
        explain("F Contradicts.")
        return
    }

    /*
    Implements this algorithm:
    1. While there are sudokus to solve:
    1.1. Pick a sudoku to solve; remove it from the list
    1.2. Set all unset cells that have only one possibility
    1.3. Recalculate possibilities
    1.4. If there are unset cells with only one possibility: go to 1.2
    1.5. If the sudoku is solved: put it to the stack of solutions and exit
    1.6. If there are no unset cells: mark as unsolvable and go to 1.1
    1.7. If any of the unset cells has no possible values: mark as unsolvable and go to 1.1
    1.8. For all possible values of a random unset cell:
    1.8.1. Copy the sudoku
    1.8.2. in the copy: set the current cell to the current value
    1.8.3. Attempt to solve the given sudoku. If a solution is found, quit
    */

    val solutionCanvas = Canvas(sudoku)

    // 1.2
    solutionCanvas.recalculatePossibleValues()
    var unsetCells = solutionCanvas.unsetCells
    var cellFound = false
    oneTwo@ while (unsetCells.isNotEmpty()) { // while implements 1.4
        cellFound = false
        for (cell in unsetCells) {
            if (cell.possibleValues.hasNone) {
                // 1.7: this is hopeless
                explain("L " + solutionCanvas.toSudoku().toOneLinerString())
                explain("F Cell $cell has no possible values.")
                return
            }
            if (cell.possibleValues.hasExactlyOne) {
                cell.set(cell.possibleValues.first)
                cellFound = true
            }
        }

        if (!cellFound) {
            // all of the unset cells have more than 1 possible value
            // force goto to 1.5
            break@oneTwo
        }

        // 1.3
        solutionCanvas.recalculatePossibleValues()
        unsetCells = solutionCanvas.unsetCells // recalculate unset cells
    }

    explain("L " + solutionCanvas.toSudoku().toOneLinerString())

    if (unsetCells.isEmpty()) {
        // all cells filled
        val solution = solutionCanvas.toSudoku()
        // 1.6
        if (solution.containsContradictions) {
            // i dont know how we got here... the @oneTwo loop should not have finished unless there are unset
            // cells or there is a determinate solution ...
            explain("F Contradicts.")
            return
            // 1.5
        } else if (solution.isComplete) {
            explain("S Solved.")
            return
        }
    }

    // 1.7 has been tackled earlier
    // 1.8
    var cell = unsetCells.first()
    explain("B $cell: ${cell.possibleValues}")
    for (value in Sudoku.FieldValue.values()) {
        if (value == Sudoku.FieldValue.UNSET || value !in cell.possibleValues) continue

        explain("G ${value.number}:")

        // 1.8.1.
        val values = solutionCanvas.toSudoku().copyOfValues()
        // 1.8.2.
        values[cell.targetRow, cell.targetColumn] = value
        // 1.8.3.
        solveAndExplain(Sudoku.of(values), out, indentLevel + 1)
    }

    // no solution found
    explain("Tree Done")
}