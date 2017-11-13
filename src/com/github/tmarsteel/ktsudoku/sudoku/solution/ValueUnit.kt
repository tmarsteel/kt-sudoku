package com.github.tmarsteel.ktsudoku.sudoku.solution

import com.github.tmarsteel.ktsudoku.sudoku.Sudoku

/**
 * A [ValueUnit] is the set of cells in a [Sudoku] that must contain all [FieldValues] from [FieldValue.ONE] to
 * [FieldValue.NINE]; these are rows, columns and areas.
 */
interface ValueUnit {
    /**
     * Whether this unit is complete (contains all required [FieldValue]s).
     */
    val isComplete: Boolean
        get() = contains(Sudoku.FieldValue.ONE)   &&
                contains(Sudoku.FieldValue.TWO)   &&
                contains(Sudoku.FieldValue.THREE) &&
                contains(Sudoku.FieldValue.FOUR)  &&
                contains(Sudoku.FieldValue.FIVE)  &&
                contains(Sudoku.FieldValue.SIX)   &&
                contains(Sudoku.FieldValue.SEVEN) &&
                contains(Sudoku.FieldValue.EIGHT) &&
                contains(Sudoku.FieldValue.NINE)

    /**
     * Returns true if this [ValueUnit] contains a [Cell] for which the given value has already been set; false otherwise.
     */
    operator fun contains(value: Sudoku.FieldValue): Boolean

    companion object {
        fun rowOf(cell: Canvas.Cell): ValueUnit = RowValueUnit(cell.sudoku, cell.targetRow)

        fun columnOf(cell: Canvas.Cell): ValueUnit = ColumnValueUnit(cell.sudoku, cell.targetColumn)

        fun areaOf(cell: Canvas.Cell): ValueUnit =
            AreaValueUnit(cell.sudoku, (cell.targetRow / 3) * 3, (cell.targetColumn / 3) * 3)
    }

    private class RowValueUnit(private val sudoku: Canvas, private val rowIndex: Int) : ValueUnit {
        init {
            if (rowIndex < 0 || rowIndex > 8) {
                throw IllegalArgumentException("Row index out of range")
            }
        }

        override operator fun contains(value: Sudoku.FieldValue): Boolean {
            for (column in 0..8) {
                if (sudoku[rowIndex, column].value == value) {
                    return true
                }
            }
            return false
        }
    }

    private class ColumnValueUnit(private val sudoku: Canvas, private val columnIndex: Int) : ValueUnit {
        init {
            if (columnIndex < 0 || columnIndex > 8) {
                throw IllegalArgumentException("Column index out of range")
            }
        }

        override operator fun contains(value: Sudoku.FieldValue): Boolean {
            for (row in 0..8) {
                if (sudoku[row, columnIndex].value == value) {
                    return true
                }
            }
            return false
        }
    }

    private class AreaValueUnit(
        private val sudoku: Canvas,
        private val topLeftCellRow: Int,
        private val topLeftCellColumn: Int
    ) : ValueUnit {
        init {
            if (topLeftCellRow < 0 || topLeftCellRow > 8) {
                throw IllegalArgumentException("Row index out of range")
            }
            if (topLeftCellColumn < 0 || topLeftCellColumn > 8) {
                throw IllegalArgumentException("Column index out of range")
            }
        }

        override operator fun contains(value: Sudoku.FieldValue): Boolean {
            for (row in 0..2) {
                for (col in 0..2) {
                    if (sudoku[topLeftCellRow + row, topLeftCellColumn + col].value == value) {
                        return true
                    }
                }
            }

            return false
        }
    }
}