package com.template.model;

import static com.template.model.XoState.E;
import static com.template.model.XoState.O;
import static com.template.model.XoState.X;

import net.corda.core.serialization.CordaSerializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@CordaSerializable
public class XoGameField {

    private final XoState cells[][];

    public XoGameField() {
        this(new XoState[][]{
            {E, E, E},
            {E, E, E},
            {E, E, E}
        });
    }

    public XoGameField(XoState[][] cells) {
        this.cells = cells;
    }

    public XoState get(int row, int col) {
        return cells[row][col];
    }

    public void set(int row, int col, XoState state) {
        cells[row][col] = state;
    }

    public XoWinner determineWinner() {
        for (int i = 0; i < 3; i++) {
            if (checkRow(i, X)) {
                return XoWinner.X_WIN;
            }
            if (checkRow(i, O)) {
                return XoWinner.O_WIN;
            }
            if (checkCol(i, X)) {
                return XoWinner.X_WIN;
            }
            if (checkCol(i, O)) {
                return XoWinner.O_WIN;
            }
        }
        if (checkDiags(X)) {
            return XoWinner.X_WIN;
        }
        if (checkDiags(O)) {
            return XoWinner.O_WIN;
        }

        return XoWinner.NONE;
    }

    private boolean checkRow(int row, XoState state) {
        for (int i = 0; i < 3; i++) {
            if (cells[row][i] != state) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCol(int col, XoState state) {
        for (int i = 0; i < 3; i++) {
            if (cells[i][col] != state) {
                return false;
            }
        }
        return true;
    }

    private boolean checkDiags(XoState state) {
        if (cells[1][1] != state) {
            return false;
        }
        return (cells[0][0] == state && cells[2][2] == state) || (cells[2][0] == state && cells[0][2] == state);
    }

    public boolean checkIsOnlyOneCellChanged(XoGameField gameField) {
        boolean oneChangeFound = false;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (this.get(row, col) != gameField.get(row, col)) {
                    if (oneChangeFound) {
                        return false;
                    }
                    oneChangeFound = true;
                }
            }
        }
        return oneChangeFound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XoGameField that = (XoGameField) o;

        return new EqualsBuilder()
            .append(cells, that.cells)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(cells)
            .toHashCode();
    }
}
