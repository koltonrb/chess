package chess;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public ChessPosition(String pos){
        // assumes non-null and proper length (letter + number for colRow notation)
        char colTemp = Character.toUpperCase(pos.charAt(0));
        int rowTemp = pos.charAt(1) - '0';  //magical arrangement of ascii codes!

        // convert letter to column row number (1 -- 8)
        HashMap<Character, Integer> nameToNum = new HashMap<>();
        nameToNum.put('A', 1);
        nameToNum.put('B', 2);
        nameToNum.put('C', 3);
        nameToNum.put('D', 4);
        nameToNum.put('E', 5);
        nameToNum.put('F', 6);
        nameToNum.put('G', 7);
        nameToNum.put('H', 8);

        this.row = rowTemp;  // to convert to row number (1 -- 8)
        this.col = nameToNum.get(colTemp);

    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
