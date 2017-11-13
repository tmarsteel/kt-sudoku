package com.github.tmarsteel.ktsudoku.sudoku.solution

import com.github.tmarsteel.ktsudoku.sudoku.Matrix2D
import com.github.tmarsteel.ktsudoku.sudoku.Sudoku

/**
 * Solution drawing board / canvas for [Sudoku]s.
 */
class Canvas
constructor(
    sudoku: Sudoku
) {
    private val fieldValues: Matrix2D<Sudoku.FieldValue>
    private val cells: Matrix2D<Cell>

    init {
        // initialize mutable field values from given sudoku
        fieldValues = Matrix2D(9, 9, { x, y -> sudoku[x, y] })

        // initialize cell helpers
        cells = Matrix2D(9, 9, { row, col -> Cell(row, col)})

        recalculatePossibleValues()
    }

    operator fun get(row: Int, col: Int) = cells[row, col]
    operator fun set(row: Int, col: Int, value: Sudoku.FieldValue) {
        if (cells[row, col].hasBeenSet) {
            throw IllegalStateException("Cell state already set")
        }
        synchronized(fieldValues) {
            fieldValues[row, col] = value
        }
    }

    fun recalculatePossibleValues() {
        for (row in 0..8) {
            for (col in 0..8) {
                cells[row, col].recalculatePossibleValues()
            }
        }
    }

    /**
     * Calculates all the unset cells
     */
    val unsetCells: Set<Cell>
        get() {
            synchronized(fieldValues) {
                val cells = mutableSetOf<Cell>()

                for (row in 0..8) {
                    for (column in 0..8) {
                        if (!this[row, column].hasBeenSet) {
                            cells.add(this[row, column])
                        }
                    }
                }

                return cells
            }
        }

    /**
     * Copies the state of this canvas to a new [Sudoku] and returns that sudoku
     */
    fun toSudoku(): Sudoku = Sudoku.of(fieldValues)

    inner class Cell
    constructor(
        val targetRow: Int,
        val targetColumn: Int
    ) {

        /**
         * The possible values of this cell. If it has already been set, returns a set consisting only of the value.
         */
        var possibleValues = FieldValueSet()
        fun recalculatePossibleValues() {
            Sudoku.FieldValue.forEach { number ->
                possibleValues[number] = number !in column && number !in row && number !in area
            }
        }

        val hasBeenSet: Boolean
            get() = value != Sudoku.FieldValue.UNSET

        val sudoku: Canvas = this@Canvas

        val value: Sudoku.FieldValue
            get() = this@Canvas.fieldValues[targetRow, targetColumn]

        val column: ValueUnit by lazy { ValueUnit.columnOf(this) }
        val row: ValueUnit by lazy { ValueUnit.rowOf(this) }
        val area: ValueUnit by lazy { ValueUnit.areaOf(this) }

        fun set(v: Sudoku.FieldValue) {
            if (v == Sudoku.FieldValue.UNSET) {
                throw IllegalArgumentException()
            }
            this@Canvas[targetRow, targetColumn] = v
        }

        override fun toString() = "(${targetRow + 1},${targetColumn + 1})"
    }
}