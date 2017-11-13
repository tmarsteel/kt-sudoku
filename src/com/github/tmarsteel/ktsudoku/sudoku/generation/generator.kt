package com.github.tmarsteel.ktsudoku.sudoku.generation

import com.github.tmarsteel.ktsudoku.sudoku.Matrix2D
import com.github.tmarsteel.ktsudoku.sudoku.Sudoku
import com.github.tmarsteel.ktsudoku.sudoku.solution.Canvas
import com.github.tmarsteel.ktsudoku.sudoku.solution.FieldValueSet
import com.github.tmarsteel.ktsudoku.sudoku.solution.Solver
import java.lang.IllegalStateException
import java.security.SecureRandom

class Generator(
        numberOfPredefinedCells: IntRange = 15..35,
        seed: ByteArray? = null
) {
    val numberOfPredefinedCells = numberOfPredefinedCells.assertAscending

    val initialSeed = seed ?: SecureRandom.getSeed(10)

    init {
        if (this.numberOfPredefinedCells.first < 0 || this.numberOfPredefinedCells.endInclusive <= 0) {
            throw IllegalArgumentException("The number of solutions must be a positive range.")
        }
    }

    private val rng = SecureRandom(initialSeed)

    fun generate(): Sudoku {
        var nCellsPrefilled = 0

        // find an unsolved sudoku that has at least 1 solution
        var sudoku = Sudoku.of(Matrix2D(9, 9, { _, _ -> Sudoku.FieldValue.UNSET }))
        do {
            while (nCellsPrefilled < numberOfPredefinedCells.first || sudoku.numberOfSolutions > 64) {
                val tryoutCanvas = Canvas(sudoku)
                val cell = tryoutCanvas.unsetCells.pickRandom(rng)!!
                cell.set(cell.possibleValues.pickRandom(rng)!!)

                val tryoutSudoku = tryoutCanvas.toSudoku()
                if (tryoutSudoku.numberOfSolutions > 0) {
                    nCellsPrefilled++
                    sudoku = tryoutSudoku
                }
            }
        }
        while(sudoku.numberOfSolutions < 1)

        if (sudoku.numberOfSolutions.equals(1) && numberOfPredefinedCells.first == numberOfPredefinedCells.last && numberOfPredefinedCells.first == nCellsPrefilled) {
            return sudoku
        }

        // work towards one of up to three solutions
        // fill the unset cell with a correct value that results in the least leftover solutions
        val solutions = sudoku.solutions.toList().pickRandom(3, rng)
        for (solution in solutions) {
            val canvas = Canvas(sudoku)
            while ((canvas.toSudoku().numberOfSolutions > 1 || nCellsPrefilled < numberOfPredefinedCells.first) && nCellsPrefilled < numberOfPredefinedCells.last) {
                val targetCellValue = canvas.unsetCells
                        .flatMap { cell ->
                            cell.recalculatePossibleValues()
                            cell.possibleValues.toIterable().map{Pair(cell, it)}
                        }
                        .map { cellAndValue ->
                            val tryoutCanvas = Canvas(canvas.toSudoku())
                            tryoutCanvas[cellAndValue.first.targetRow, cellAndValue.first.targetColumn] = cellAndValue.second
                            Pair(cellAndValue, tryoutCanvas.toSudoku().numberOfSolutions)
                        }
                        .filter { it.second > 0 }
                        .minBy { it.second }
                        ?.first

                if (targetCellValue != null) {
                    canvas[targetCellValue.first.targetRow, targetCellValue.first.targetColumn] = targetCellValue.second
                }
            }

            val possibleResultSudoku = canvas.toSudoku()
            if (possibleResultSudoku.numberOfSolutions.equals(1)) {
                return possibleResultSudoku
            }
        }

        // all solutions depleted ... the RNG gods were not on our side :(
        return generate()
    }
}

private operator fun IntRange.contains(int: DeferredSequenceSizeInt) = int >= first && int <= last

class DeferredSequenceSizeInt(val sequence: Sequence<*>): Comparable<DeferredSequenceSizeInt> {
    private var knownSize = 0

    private val iterator = sequence.iterator()

    operator fun compareTo(other: Int): Int {
        if (knownSize > other) {
            return 1
        }

        advanceKnownSizeUpTo(knownSize + 1)

        if (knownSize > other) {
            return 1
        }

        advanceKnownSizeUpTo(other + 1)

        return knownSize - other
    }

    fun equals(other: Int): Boolean {
        advanceKnownSizeUpTo(knownSize + 1)
        return knownSize == other
    }

    override operator fun compareTo(other: DeferredSequenceSizeInt): Int {
        advanceFully()
        other.advanceFully()

        return this.knownSize - other.knownSize
    }

    private fun advanceFully() {
        while (iterator.hasNext()) {
            knownSize++
            iterator.next()
        }
    }

    private fun advanceKnownSizeUpTo(target: Int) {
        try {
            while (iterator.hasNext() && knownSize < target) {
                knownSize++
                iterator.next()
            }
        }
        catch (ex: Solver.UnsolvableSudokuException) {
            return
        }
        catch (ex: IllegalStateException) {
            return
        }
    }
}

private fun FieldValueSet.pickRandom(sourceOfRandomness: SecureRandom = SecureRandom()): Sudoku.FieldValue? {
    if (hasNone) return null

    return Sudoku.FieldValue.values().filter{ it in this }.toTypedArray().pickRandom(sourceOfRandomness)
}

private fun FieldValueSet.toIterable(): Iterable<Sudoku.FieldValue> {
    val fvset = this
    return object: Iterable<Sudoku.FieldValue> {
        override fun iterator() = Sudoku.FieldValue.values().filter(fvset::contains).iterator()
    }
}

private inline fun <reified T> Array<T>.pickRandom(sourceOfRandomness: SecureRandom = SecureRandom()): T? {
    if (isEmpty()) return null

    return this[sourceOfRandomness.nextInt(size)]
}

private inline fun <reified T> Collection<T>.pickRandom(sourceOfRandomness: SecureRandom = SecureRandom()): T? = toTypedArray().pickRandom(sourceOfRandomness)

private inline fun <reified T> Collection<T>.pickRandom(n: Int, sourceOfRandomness: SecureRandom = SecureRandom()): Collection<T> {
    if (n < 0) throw IllegalArgumentException()

    if (size <= n) {
        return this
    }

    val asList = toList()
    val target = ArrayList<T>(n)
    val indizesPicked = Array(n, { -1 })
    var nPicked = 0
    while (nPicked < n) {
        val index = sourceOfRandomness.nextInt(size)
        if (index !in indizesPicked) {
            indizesPicked[nPicked] = index
            target.add(asList[index])
            nPicked++
        }
    }

    return target
}

private val IntRange.assertAscending: IntRange
    get() = if (first < endInclusive) this else endInclusive..first