package chess;

public class PieceMovesCalculator {

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {

    }

    public calculateDiagonalMoves(ChessBoard board, ChessPosition position) {

        private Final Integer numRows;
        private Final Integer numCols;
        private HashSet<int> rows;
        private HashSet<int> cols;

        numRows = board.length;
        numCols = board[0].length;
        rows = fillGridOptions(min=0, max=numRows, current=position.getRow());
        cols = fillGridOptions(min=0, max=numCols, current=position.getCol());


    }

    private HashSet<int> fillGridOptions(int min, int max, int current) {
        HashSet<Integer> vals;

        vals = new HashSet<>();

        for (int i = min; i < max + 1; i++){
            vals.add(i);
        }
        vals.remove(current); // we just want a list of spaces to which we might be able to move
        return vals;
    }

    public static ChessMove pieceMoves(ChessBoard board, ChessPosition position) {

    }
}
