package com.github.tmarsteel.ktsudoku.sudoku.solution

import com.github.tmarsteel.ktsudoku.sudoku.Sudoku
import kotlin.coroutines.experimental.buildSequence

/**
 * Solves [Sudoku]s and returns the solutions as [Sudoku]s.
 */
class Solver {
    /**
     * Attempts to solve the given sudoku. Returns a solution, if one is found, null otherwise.
     */
    fun solve(sudoku: Sudoku): Sequence<Sudoku> = buildSequence {
        if (sudoku.containsContradictions) {
            throw UnsolvableSudokuException("Contains contradicting pre filled values.")
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
                    return@buildSequence
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

        if (unsetCells.isEmpty()) {
            // all cells filled
            val solution = solutionCanvas.toSudoku()
            // 1.6
            if (solution.containsContradictions) {
                // i dont know how we got here... the @oneTwo loop should not have finished unless there are unset
                // cells or there is a determinate solution ...
                return@buildSequence
            // 1.5
            } else if (solution.isComplete) {
                yield(solution)
                return@buildSequence
            }
        }

        // 1.7 has been tackled earlier
        // 1.8
        var cell = unsetCells.first()
        for (value in Sudoku.FieldValue.values()) {
            if (value == Sudoku.FieldValue.UNSET || value !in cell.possibleValues) continue

            // 1.8.1.
            val values = solutionCanvas.toSudoku().copyOfValues()
            // 1.8.2.
            values[cell.targetRow, cell.targetColumn] = value
            // 1.8.3.
            try {
                yieldAll(solve(Sudoku.of(values)))
            } catch (ex: UnsolvableSudokuException) {}
        }

        // no solution found
    }

    /**
     * Thrown when a sudoku should be solved that is not solvable
     */
    class UnsolvableSudokuException(reason: String) : Exception(reason) {}
}