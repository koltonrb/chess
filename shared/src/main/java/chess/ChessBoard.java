package chess;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    public ChessBoard( ChessBoard original ){
        for (int row=1; row<=8; row++){
            for (int col=1; col<=8; col++){
                ChessPosition mySquare = new ChessPosition(row, col);
                if (original.getPiece( mySquare ) != null) {
                    addPiece( mySquare, new ChessPiece( original.getPiece( mySquare ) ));
                }
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece; // subtract 1 to account for zero indexing
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // set up the black pieces
        squares[7][0] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        squares[7][1] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        squares[7][2] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        squares[7][3] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        squares[7][4] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        squares[7][5] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        squares[7][6] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        squares[7][7] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        for (int col=0; col < 8; col++){
            squares[6][col] = new ChessPiece( ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }

        // ditto for white pieces
        squares[0][0] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        squares[0][1] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        squares[0][2] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        squares[0][3] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        squares[0][4] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        squares[0][5] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        squares[0][6] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        squares[0][7] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        for (int col=0; col < 8; col++){
            squares[1][col] = new ChessPiece( ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public String toString(){
        Map<ChessPiece.PieceType, String> PIECE_TO_CHAR = Map.of(
                 ChessPiece.PieceType.PAWN, "p",
                 ChessPiece.PieceType.KNIGHT, "n",
                 ChessPiece.PieceType.ROOK, "r",
                 ChessPiece.PieceType.QUEEN, "q",
                 ChessPiece.PieceType.KING, "k",
                 ChessPiece.PieceType.BISHOP, "b" );

        StringBuilder toPrintOut = new StringBuilder();
        for (int row = 8; row >= 1; row--){
            // start at the top of the board and work down
            // so that Black is always in rows 7 and 8
            for (int col = 1; col <= 8; col++){
                toPrintOut.append('|');
                String myStr = " ";
                ChessPosition mySquare = new ChessPosition(row, col);

                if ((getPiece(mySquare) != null) && (getPiece(mySquare).getTeamColor() == ChessGame.TeamColor.WHITE)){
                    myStr = PIECE_TO_CHAR.get( getPiece(mySquare).getPieceType() );
                    myStr = String.valueOf(Character.toUpperCase( myStr.charAt(0) ));
                } else if ((getPiece(mySquare) != null) && (getPiece(mySquare).getTeamColor() == ChessGame.TeamColor.BLACK)){
                    myStr = PIECE_TO_CHAR.get( getPiece(mySquare).getPieceType() );
                } else {
                    myStr = " ";
                }
                toPrintOut.append(myStr);
            }
            toPrintOut.append("|\n");
        }
        return toPrintOut.toString();
    }
}
