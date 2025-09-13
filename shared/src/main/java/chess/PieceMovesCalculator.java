package chess;

import java.util.ArrayList;

public class PieceMovesCalculator {

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {

    }

    public ArrayList<ChessMove> calculateDiagonalMoves(ChessBoard board, ChessPosition position) {

        int numRows;
        int numCols;
        int[] rows;
        int[] cols;
        ArrayList<ChessMove> diagonalMoves;

        numRows = 8;
        numCols = 8;
        diagonalMoves = new ArrayList<ChessMove>();
        rows = fillGridOptions(1, numRows);
        cols = fillGridOptions(1, numCols);

        // make a list of ordered pairs (row, col) to consider for diagonal moves
        for (int i = 0; i < numRows; i++) {
            int row = rows[i];
            if (row == position.getRow()) {
                continue;  // the current row cannot be a diagonal move
            }
            for (int j = 0; j < numCols; j++) {
                int col = cols[j];
                if (col == position.getColumn()) {
                    continue; // the current col cannot be a diagonal move
                }
                if (Math.abs(position.getRow() - row) == Math.abs(position.getColumn() - col)){
                    // then (row, col) is on a diagonal from the current position
                    diagonalMoves.add(new ChessMove( position, new ChessPosition(row, col),  null));
                }

            }
        }
        return diagonalMoves;
    }

    private int[] fillGridOptions(int min, int max) {
        int[] vals;

        vals = new int[max - min + 1];

        for (int i = min; i < max + 1; i++){ // + 1 since min and max are row/col numbers, not indices
            vals[i - 1] = i;  // zero indexed array
        }
        //vals.remove(current); // we just want a list of spaces to which we might be able to move
        return vals;
    }

}
