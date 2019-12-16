package com.template.model;

import static com.template.model.XoState.E;
import static com.template.model.XoState.O;
import static com.template.model.XoState.X;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class XoGameFieldTest {

    @Test
    public void get() {
        XoGameField field = new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        });
        assertThat(field.get(2, 2), is(XoState.X));
        assertThat(field.get(0, 1), is(XoState.E));
        assertThat(field.get(1, 0), is(XoState.O));
    }

    @Test
    public void set() {
        XoGameField field = new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        });
        assertThat(field.get(2, 2), is(XoState.X));
        field.set(2, 2, O);
        assertThat(field.get(2, 2), is(XoState.O));
    }

    @Test
    public void determineWinner() {
        assertThat(new XoGameField(new XoState[][]{
            {X, E, E},
            {E, X, E},
            {E, E, X}
        }).determineWinner(), is(XoWinner.X_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {E, E, X},
            {E, X, E},
            {X, E, E}
        }).determineWinner(), is(XoWinner.X_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {X, X, X},
            {E, E, E},
            {E, E, E}
        }).determineWinner(), is(XoWinner.X_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {E, E, E},
            {X, X, X},
            {E, E, E}
        }).determineWinner(), is(XoWinner.X_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {O, E, E},
            {E, X, E},
            {O, O, O}
        }).determineWinner(), is(XoWinner.O_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {O, E, E},
            {O, X, E},
            {O, E, X}
        }).determineWinner(), is(XoWinner.O_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {X, O, E},
            {E, O, E},
            {E, O, X}
        }).determineWinner(), is(XoWinner.O_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {X, E, O},
            {E, X, O},
            {E, E, O}
        }).determineWinner(), is(XoWinner.O_WIN));
        assertThat(new XoGameField(new XoState[][]{
            {E, O, E},
            {O, X, O},
            {E, O, X}
        }).determineWinner(), is(XoWinner.NONE));
    }

    @Test
    public void testEquals() {
        XoGameField field1 = new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        });
        XoGameField field2 = new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        });
        XoGameField field3 = new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, O}
        });

        assertThat("Field equals to itself", field1.equals(field1), is(true));
        assertThat("Field equals to another same field", field1.equals(field2), is(true));
        assertThat("Field unequals to another different field", field1.equals(field3), is(false));
    }

    @Test
    public void checkIsOnlyOneCellChanged() {
        XoGameField field1 = new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        });
        XoGameField field2 = new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, O, X}
        });
        XoGameField field3 = new XoGameField(new XoState[][]{
            {E, E, E},
            {E, E, E},
            {E, E, E}
        });
        assertThat("when no cells changed", field1.checkCellChangeValidity(field1, X), is(false));
        assertThat("when no cells changed 2", field1.checkCellChangeValidity(field1, O), is(false));
        assertThat("when one cell changed", field1.checkCellChangeValidity(field2, O), is(true));
        assertThat("when one cell changed but another character expected", field1.checkCellChangeValidity(field2, X), is(false));
        assertThat("when more than one cell changed", field1.checkCellChangeValidity(field3, X), is(false));
        assertThat("when more than one cell changed 2", field1.checkCellChangeValidity(field3, O), is(false));
    }

    @Test
    public void testGameFieldCreationFromStringForStringWithWrongLength() {
        try {
            new XoGameField("---");
            fail("Exception should be thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("To create game field - 9 characters expected"));
        }
    }

    @Test
    public void testGameFieldCreationFromStringForStringWithUnknownCharacter() {
        try {
            new XoGameField("-------Z-");
            fail("Exception should be thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("Unknown character: Z"));
        }
    }

    @Test
    public void testGameFieldCreationFromString() {
        XoGameField field = new XoGameField("-O--X-OX-");
        assertThat(field.equals(new XoGameField(new XoState[][]{
            {E, O, E},
            {E, X, E},
            {O, X, E}
        })), is(true));
    }

    @Test
    public void testToPrettyPrintString() {
        XoGameField field = new XoGameField("-O--X-OX-");
        assertThat(field.toPrettyPrintString(), is(""
            + "   | O |   "
            + "---|---|---"
            + "   | X |   "
            + "---|---|---"
            + " O | X |   "));
    }

    @Test
    public void testToLinearString() {
        XoGameField field = new XoGameField("-O--X-OX-");
        assertThat(field.toString(), is("-O--X-OX-"));
    }
}
