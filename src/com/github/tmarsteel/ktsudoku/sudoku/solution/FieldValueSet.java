package com.github.tmarsteel.ktsudoku.sudoku.solution;

import com.github.tmarsteel.ktsudoku.sudoku.Sudoku;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/**
 * Utility to keep track of set/not set state of field values in units and elsewhere. Stores a bitflag with one
 * bit per field value; does not account for [FieldValue.UNSET]
 */
public class FieldValueSet {
    private int flags = 0;

    public boolean get(Sudoku.FieldValue value) {
        return (flags & (1 << value.getIndex())) != 0;
    }

    public boolean contains(Sudoku.FieldValue value) { return get(value); }

    public void set(Sudoku.FieldValue value, boolean is) {
        if (is) {
            flags |= 1 << value.getIndex();
        } else {
            flags ^= flags & (1 << value.getIndex());
        }
    }

    public boolean getHasAll() {
        return (flags & 0b1111111110) == 0b1111111110;
    }

    public boolean getHasExactlyOne() {
        // thanks to http://stackoverflow.com/questions/12483843/test-if-a-bitboard-have-only-one-bit-set-to-1
        return flags != 0 && (flags & (flags-1)) == 0;
    }

    public Sudoku.FieldValue getFirst() {
        if (getHasNone()) {
            throw new IllegalStateException("No flag set, cannot return first");
        }
        for (Sudoku.FieldValue value : Sudoku.FieldValue.values())
        {
            if (get(value)) {
                return value;
            }
        }
        return null;
    }

    public boolean getHasNone() {
        return (flags & 0b1111111110) == 0;
    }

    /**
     * Sets all flags to 0 / false
     */
    public void reset() {
        flags = 0;
    }

    @Override
    public String toString() {
        return "[" +
            Arrays.stream(Sudoku.FieldValue.values())
            .filter(this::contains)
            .map(Sudoku.FieldValue::getNumber)
            .map(Object::toString)
            .collect(joining(", "))
            + "]";
    }
}
