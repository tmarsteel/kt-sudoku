package com.github.tmarsteel.ktsudoku.sudoku

import com.github.tmarsteel.ktsudoku.sudoku.generation.DeferredSequenceSizeInt
import com.github.tmarsteel.ktsudoku.sudoku.solution.FieldValueSet
import com.github.tmarsteel.ktsudoku.sudoku.solution.Solver
import ext.collections.cached

/**
 * Represents a sudoku field (solved or unsolved). Must be immutable (field values cannot change).
 */
interface Sudoku {

    val solutions: Sequence<Sudoku>
    val numberOfSolutions: DeferredSequenceSizeInt

    /**
     * Returns the value of the field at the given row and column.
     * @param row The row to access; must be between 0 and 8
     * @param column the column to access; must be between 0 and 8
     */
    operator fun get(row: Int, column: Int): FieldValue

    /**
     * Copies the values of this sudoku to a [Matrix2D] and returns that matrix.
     */
    fun copyOfValues(): Matrix2D<FieldValue> {
        return Matrix2D(9, 9, {x, y -> this[x, y]})
    }

    /**
     * Returns true if this sudoku is not solvable because it contains invalid pre-filld values.
     * **Important:** It is not assured that this sudoku is actually solvable if this method returns false. A return
     * value of `false` should be interpreted as "is potentially solvable" or "there is nothing obvious that renders this
     * puzzle unsolvable"
     */
    val containsContradictions: Boolean
        get() {
            var valueSet = FieldValueSet()

            // validate rows
            for (row in 0..8) {
                valueSet.reset()
                for (column in 0..8) {
                    val cellValue = this[row, column]
                    if (cellValue == FieldValue.UNSET) continue
                    if (cellValue in valueSet) {
                        return true
                    }
                    valueSet[cellValue] = true
                }
            }


            // validate columns
            for (column in 0..8) {
                valueSet.reset()
                for (row in 0..8) {
                    val cellValue = this[row, column]
                    if (cellValue == FieldValue.UNSET) continue
                    if (cellValue in valueSet) {
                        return true
                    }
                    valueSet[cellValue] = true
                }
            }

            // validate 3x3 areas
            for (areaRow in 0..2) {
                for (areaColumn in 0..2) {
                    valueSet.reset()
                    for (column in 0..2) {
                        for (row in 0..2) {
                            val cellValue = this[areaRow * 3 + row, areaColumn * 3 + column]
                            if (cellValue == FieldValue.UNSET) continue
                            if (cellValue in valueSet) {
                                return true
                            }
                            valueSet[cellValue] = true
                        }
                    }
                }
            }

            // no obvious errors found => potentially solvable
            return false
        }

    val isComplete: Boolean
        get() {
            var valueSet = FieldValueSet()

            // validate rows
            for (row in 0..8) {
                valueSet.reset()
                for (column in 0..8) {
                    val cellValue = this[row, column]
                    if (cellValue == FieldValue.UNSET) {
                        return false
                    }
                    valueSet[cellValue] = true
                }
                if (!valueSet.hasAll) {
                    return false
                }
            }

            // validate columns
            for (column in 0..8) {
                valueSet.reset()
                for (row in 0..8) {
                    val cellValue = this[row, column]
                    if (cellValue == FieldValue.UNSET) {
                        return false
                    }
                    valueSet[cellValue] = true
                }
                if (!valueSet.hasAll) {
                    return false
                }
            }

            // validate 3x3 areas
            for (areaRow in 0..2) {
                for (areaColumn in 0..2) {
                    valueSet.reset()
                    for (column in 0..2) {
                        for (row in 0..2) {
                            val cellValue = this[areaRow * 3 + row, areaColumn * 3 + column]
                            if (cellValue == FieldValue.UNSET) {
                                return false
                            }
                            valueSet[cellValue] = true
                        }
                    }
                    if (!valueSet.hasAll) {
                        return false
                    }
                }
            }

            // all correct!!
            return true
        }

    enum class FieldValue(val index: Int) {
        UNSET(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9);

        /**
         * Returns the next value (with index one greater than `this`) for [UNSET] to [EIGHT], returns `null` for [NINE].
         */
        fun next(): FieldValue? {
            if (this == NINE) {
                return null
            }
            return values()[index + 1]
        }

        val number: Int = index

        companion object {
            /**
             * Invokes the given consumer for all instances of this ennum except for [UNSET]
             */
            fun forEach(consumer: (FieldValue) -> Unit) {
                var v: FieldValue? = ONE
                while (v != null) {
                    consumer(v)
                    v = v.next()
                }
            }
        }
    }

    companion object {
        /**
         * Copies the state of the given matrix to a new sudoku
         */
        fun of(origValues: Matrix2D<FieldValue>): Sudoku = MatrixSudoku(origValues)
    }

    private class MatrixSudoku(val origValues: Matrix2D<FieldValue>) : Sudoku {
        override val solutions by lazy { Solver().solve(this).cached }
        override val numberOfSolutions by lazy { DeferredSequenceSizeInt(solutions) }

        private val values: Array<Array<FieldValue>> = Array(9, {x -> Array(9, {y -> origValues[x, y]})})
        private val hashCode: Int by lazy {
            var stringRep = ""
            for (row in 0..8) {
                for (col in 0..8) {
                    stringRep += values[row][col].number
                }
            }
            stringRep.hashCode()
        }

        init {
            if (origValues.sx != 9 || origValues.sy != 9) {
                throw IllegalArgumentException("Matrix size must be 9x9")
            }
        }

        override fun get(row: Int, column: Int): FieldValue {
            return values[row][column]
        }

        override fun hashCode(): Int = hashCode

        override fun equals(other: Any?): Boolean {
            if (other is MatrixSudoku) {
                return other.hashCode() == hashCode()
            } else if (other is Sudoku) {
                for (row in 0..8) {
                    for (col in 0..8) {
                        if (other[row, col] != values[row][col]) {
                            return false
                        }
                    }
                }
                return true
            } else {
                return false
            }
        }
    }
}
