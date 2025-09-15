package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    public PieceType type;
    public ChessGame.TeamColor pieceColor;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return type == that.type && pieceColor == that.pieceColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pieceColor);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        PieceMovesCalculator movesCalculator = new PieceMovesCalculator(board, myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            return movesCalculator.calculateDiagonalMoves(board, myPosition);
        }
        if (piece.getPieceType() == PieceType.ROOK){
            return movesCalculator.calculatePerpendicularMoves(board, myPosition);
        }
        if (piece.getPieceType() == PieceType.QUEEN){
            ArrayList<ChessMove> diagonalMoves = movesCalculator.calculateDiagonalMoves(board, myPosition);
            ArrayList<ChessMove> perpendicularMoves = movesCalculator.calculatePerpendicularMoves(board, myPosition);
            diagonalMoves.addAll(perpendicularMoves);
            return diagonalMoves; // now houses both diagonal and perpendicular moves
        }
        return List.of();
    }
}
