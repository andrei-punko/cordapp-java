package com.template.model;

import static com.template.model.XoState.E;
import static com.template.model.XoState.O;
import static com.template.model.XoState.X;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        assertThat("Should be false when check vs itself", field1.checkIsOnlyOneCellChanged(field1), is(false));
        assertThat("Should be true when one cell changed", field1.checkIsOnlyOneCellChanged(field2), is(true));
        assertThat("Should be true when one cell changed 2", field2.checkIsOnlyOneCellChanged(field1), is(true));
        assertThat("Should be false when more than one cell changed", field1.checkIsOnlyOneCellChanged(field3), is(false));
        assertThat("Should be false when more than one cell changed 2", field3.checkIsOnlyOneCellChanged(field1), is(false));
    }
}
