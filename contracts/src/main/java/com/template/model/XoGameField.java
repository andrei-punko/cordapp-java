package com.template.model;

import static com.template.model.XoState.E;
import static com.template.model.XoState.O;
import static com.template.model.XoState.X;

import net.corda.core.serialization.ConstructorForDeserialization;
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

    @ConstructorForDeserialization
    public XoGameField(XoState[][] cells) {
        this.cells = cells;
    }

    public XoGameField(String str) {
        if (str.length() != 9) {
            throw new IllegalArgumentException("To create game field - 9 characters expected");
        }
        cells = new XoState[3][3];
        int row = 0;
        int col = 0;
        for (Character ch : str.toCharArray()) {
            switch (ch) {
                case 'X':
                    cells[row][col] = X;
                    break;
                case 'O':
                    cells[row][col] = O;
                    break;
                case '-':
                    cells[row][col] = E;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown character: " + ch);
            }
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
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

    @Override
    public String toString() {
        String result = "";
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                char ch = ' ';
                switch (cells[row][col]) {
                    case X:
                        ch = 'X';
                        break;
                    case O:
                        ch = 'O';
                        break;
                }
                result += String.format(" %s |", ch);
            }
            result = result.substring(0, result.length() - 1);
            result += "---|---|---";
        }
        return result.substring(0, result.length() - 11);
    }

    public String toLinearString() {
        String result = "";
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                switch (cells[row][col]) {
                    case E:
                        result += '-';
                        break;
                    case X:
                        result += 'X';
                        break;
                    case O:
                        result += 'O';
                        break;
                }
            }
        }
        return result;
    }
}
