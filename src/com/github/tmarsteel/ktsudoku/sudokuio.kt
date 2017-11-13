package com.github.tmarsteel.ktsudoku

import com.github.tmarsteel.ktsudoku.sudoku.Matrix2D
import com.github.tmarsteel.ktsudoku.sudoku.Sudoku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream

val stdin = BufferedReader(InputStreamReader(System.`in`))
val stdout = System.out

fun BufferedReader.readSudoku(): Sudoku {
    val values = Array(81, { Sudoku.FieldValue.UNSET})
    var nValuesRead = 0

    while (nValuesRead < 81) {
        val input = this.readLine()!!
        input.split(',', ';').forEach { number ->
            val char = number[0]
            if (!char.isWhitespace()) {
                if (char.isDigit()) {
                    values[nValuesRead++] = Sudoku.FieldValue.values()[Integer.valueOf(char.toString())]
                }
                else if (char == '-') {
                    values[nValuesRead++] = Sudoku.FieldValue.values()[0]
                }
                else {
                    throw RuntimeException("Please input only digits")
                }

                if (nValuesRead > 81) {
                    throw RuntimeException("Too much input")
                }
            }
        }
    }

    val matrix = Matrix2D(9, 9, {row, col -> values[row * 9 + col]})
    return Sudoku.of(matrix)
}

fun Sudoku.toPrettyString(): String {
    var out = ""

    for (row in 0..8) {
        for (col in 0..8) {
            out += this[row, col].number
            if ((col + 1) % 3 == 0) out += " "
        }
        out += "\n"
        if ((row + 1) % 3 == 0) out += "\n"
    }

    return out
}

fun Sudoku.toOneLinerString(): String {
    var out = ""

    for (row in 0..8) {
        for (col in 0..8) {
            val value = this[row, col]
            if (value == Sudoku.FieldValue.UNSET) {
                out += "-"
            }
            else {
                out += value.number
            }
            if (col < 8) out += ","
        }
        if (row < 8) out += ";"
    }

    return out
}

fun PrintStream.printSudoku(sudoku: Sudoku) {
    this.println(sudoku.toPrettyString())
}

fun PrintStream.printSudokuAsOneLiner(sudoku: Sudoku) {
    this.print(sudoku.toOneLinerString());
}

fun ask(question: String, answerPredicate: (String) -> Boolean): String {
    while (true) {
        println(question)

        val answer = readLine()
        if (answer != null && answerPredicate(answer)) {
            return answer
        }

        println("Invalid input")
    }
}

fun askYesNo(question: String): Boolean {
    val answers = mapOf(
        "y" to true,
            "yes" to true,
            "n"   to false,
            "no"  to false
    )
    return answers[ask(question + " (yes/no)", { answer -> answer in answers.keys })]!!
}