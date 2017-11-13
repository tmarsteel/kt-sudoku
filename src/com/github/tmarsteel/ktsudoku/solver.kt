package com.github.tmarsteel.ktsudoku

import com.github.tmarsteel.ktsudoku.sudoku.solution.Solver

fun main(args: Array<String>) {
    println("Input a sudoku; type 0 or - for unset fields")
    val sudoku = stdin.readSudoku()
    val solver = Solver()


    val solutions = try {
        solver.solve(sudoku).iterator()
    } catch (ex: Solver.UnsolvableSudokuException) {
        System.err.println("The sudoku cannot be solved: " + ex.message)
        return
    }

    var nSolutions = 0
    for (solution in solutions) {
        stdout.println()
        stdout.printSudoku(solution)
        stdout.printSudokuAsOneLiner(solution)
        stdout.println()
        stdout.println()
        nSolutions++
    }

    stdout.println()
    stdout.println("Found $nSolutions solution(s)")
}

