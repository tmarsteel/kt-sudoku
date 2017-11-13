package com.github.tmarsteel.ktsudoku

import com.github.tmarsteel.ktsudoku.sudoku.Sudoku
import com.github.tmarsteel.ktsudoku.sudoku.generation.Generator
import com.github.tmarsteel.ktsudoku.sudoku.solution.Canvas
import com.github.tmarsteel.ktsudoku.sudoku.solution.FieldValueSet
import java.math.BigInteger
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream
import kotlin.coroutines.experimental.buildSequence

fun main(args: Array<String>) {
    val nSudokus = Integer.parseInt(ask("Number of Sudokus to generate:", String::isInteger))

    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "3")
    val sudokus = findSudokusWithSimpleBacktracking().limit(nSudokus.toLong())

    for (sudoku in sudokus) {
        //solveAndExplain(sudoku, stdout)
        println(sudoku.toOneLinerString())
        println()
    }
}

fun findSudokusWithSimpleBacktracking(): Stream<Sudoku> {
    val generator = Generator(
            numberOfPredefinedCells = 25..50
    )

    println("Generator seed: " + generator.initialSeed.hexNotation)

    val sudokus = Stream.generate {
        log("Generating sudoku...")
        val start = System.currentTimeMillis()
        val value = generator.generate()
        val duration = System.currentTimeMillis() - start
        log("Generated one sudoku, took $duration milliseconds")
        value
    }

    /*val sudokusWithDeduction = sudokus.map { Pair(it, it.logicallyDeducted) }

    val sudokusRequiringBacktracking = sudokusWithDeduction.filter { sudokuWithDeduction ->
        val isLogicallyDeductable = sudokuWithDeduction.second.isComplete

        if (isLogicallyDeductable) {
            log("Ditching 1 sudoku because it is logically deductable")
        }

        !isLogicallyDeductable
    }*/

    /*val sudokusWithMostSimpleBacktracking = sudokusRequiringBacktracking.filter { sudokuWithDeduction ->
        // backtrack + logically deduct cell 1
        val backtrackingStep1 = backtrackLeftmostTopmost(sudokuWithDeduction.second)

        if (backtrackingStep1.any { it.isComplete }) {
            log("Ditching 1 sudoku because it can be solved by backtracking only one cell")
            return@filter false
        }

        val backtrackingStep2 = backtrackingStep1.flatMap(::backtrackLeftmostTopmost)

        val hasSolutionsForTwo = backtrackingStep2.any { it.isComplete }

        if (hasSolutionsForTwo) {
            return@filter true
        }

        val backtrackingStep3 = backtrackingStep2.flatMap(::backtrackLeftmostTopmost)

        val hasSolutionsForThree = backtrackingStep3.any { it.isComplete }

        if (!hasSolutionsForThree) {
            log("Ditching 1 sudoku because it cannot be solved by backtracking only two or three cells")
        }

        hasSolutionsForThree
    }*/

    return sudokus.map { it }
}

private fun backtrackLeftmostTopmost(sudoku: Sudoku) = buildSequence {
    val canvas = Canvas(sudoku)
    val leftmostTopmostUnsetCell = canvas.leftmostTopmostUnsetCell

    // backtracking cell 1
    for (value in leftmostTopmostUnsetCell.possibleValues.toIterable()) {
        val canvasCopy = canvas.copy()
        canvasCopy[leftmostTopmostUnsetCell.targetRow, leftmostTopmostUnsetCell.targetColumn].set(value)

        yield(canvasCopy.toSudoku().logicallyDeducted)
    }
}

private val Sudoku.logicallyDeducted: Sudoku
    get() {
        // do logical deduction
        val canvas = Canvas(this)
        while (canvas.containsCellsWithOnlyOneOption) {
            for (cell in canvas.unsetCells) {
                cell.recalculatePossibleValues()
                if (cell.possibleValues.hasExactlyOne) {
                    cell.set(cell.possibleValues.first)
                }
            }
        }

        return canvas.toSudoku()
    }

private fun Canvas.copy() = Canvas(this.toSudoku())

private val Canvas.leftmostTopmostUnsetCell: Canvas.Cell
    get() {
        val unsetCells = unsetCells

        if (unsetCells.isEmpty()) {
            log("Filled canvas given -- yieck!")
            throw IllegalArgumentException()
        }

        return unsetCells.sortedWith(compareBy(Canvas.Cell::targetRow, Canvas.Cell::targetColumn)).first()
    }

private fun FieldValueSet.toIterable(): Iterable<Sudoku.FieldValue> {
    val fvset = this
    return object: Iterable<Sudoku.FieldValue> {
        override fun iterator() = Sudoku.FieldValue.values().filter(fvset::contains).iterator()
    }
}

private val ByteArray.hexNotation: String
    get() = BigInteger(this).toString(16)

private val String.isInteger: Boolean
    get() = try {
            Integer.parseInt(this)
            true
        }
        catch (ex: NumberFormatException) {
            false
        }

private fun log(msg: String) {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    println("[Thread ${Thread.currentThread().id} ${now.format(DateTimeFormatter.ISO_DATE_TIME)}] $msg")
}