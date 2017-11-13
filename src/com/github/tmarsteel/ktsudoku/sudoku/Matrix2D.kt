package com.github.tmarsteel.ktsudoku.sudoku

class Matrix2D<T: Any>(val sx: Int, val sy: Int, initValue: (Int, Int) -> T) {

    private val values: Array<Array<T>>

    init {
        if (sx < 0) {
            throw IllegalArgumentException("Negative x size")
        }
        if (sy < 0) {
            throw IllegalArgumentException("Negative y size")
        }

        val untypedValues: Array<Array<*>> = Array(sx, {xIndex -> Array<Any>(sy, {yIndex -> initValue(xIndex, yIndex)})})
        values = untypedValues as Array<Array<T>>
    }

    operator fun get(x: Int, y: Int): T = values[x][y]

    operator fun set(x: Int, y: Int, v: T): Unit { values[x][y] = v }
}