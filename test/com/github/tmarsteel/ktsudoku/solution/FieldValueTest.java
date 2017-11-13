package com.github.tmarsteel.ktsudoku.solution;

import com.github.tmarsteel.ktsudoku.sudoku.Sudoku;
import com.github.tmarsteel.ktsudoku.sudoku.solution.FieldValueSet;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FieldValueTest
{
    private FieldValueSet subject;

    @Before
    public void setUp() {
        subject = new FieldValueSet();
    }

    @Test
    public void test_getAndSet()
    {
        for (Sudoku.FieldValue value : Sudoku.FieldValue.values())
        {
            if (value == Sudoku.FieldValue.UNSET) continue;

            subject.set(value, true);
            assertTrue(subject.get(value));
            subject.set(value, false);
            assertFalse(subject.get(value));
        }
    }

    @Test
    public void test_getHasExactlyOne() {
        for (Sudoku.FieldValue value : Sudoku.FieldValue.values())
        {
            if (value == Sudoku.FieldValue.UNSET) continue;

            subject.reset();
            subject.set(value, true);
            assertTrue(subject.getHasExactlyOne());

            Sudoku.FieldValue next = value.next() == null? Sudoku.FieldValue.ONE : value.next();
            subject.set(next, true);
            assertFalse(subject.getHasExactlyOne());
        }
    }

    @Test
    public void test_getFirst() {
        subject.set(Sudoku.FieldValue.FIVE, true);
        subject.set(Sudoku.FieldValue.NINE, true);
        assertSame(Sudoku.FieldValue.FIVE, subject.getFirst());

        subject.reset();
        subject.set(Sudoku.FieldValue.ONE, true);
        subject.set(Sudoku.FieldValue.SIX, true);
        assertSame(Sudoku.FieldValue.ONE, subject.getFirst());


        subject.reset();
        subject.set(Sudoku.FieldValue.THREE, true);
        subject.set(Sudoku.FieldValue.SEVEN, true);
        subject.set(Sudoku.FieldValue.FOUR, true);
        assertSame(Sudoku.FieldValue.THREE, subject.getFirst());
    }
}
