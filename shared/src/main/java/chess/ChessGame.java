package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor whoseTurn;
    private ChessBoard board;

    public ChessGame() {
        this.whoseTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }
    public ChessGame(ChessGame original){
        this.whoseTurn = original.getTeamTurn();
        this.board = new ChessBoard( original.getBoard() );
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.whoseTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.whoseTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // a move is valid if it
        // has a chess piece at the specified start position
        if ( this.getBoard().getPiece( move.getStartPosition() ) == null){
            throw new InvalidMoveException(String.format("There is no piece to move at %s", move.getStartPosition().toString() ));
        }
        // check that any piece at the start position does actually belong to your color
        if (( this.getBoard().getPiece( move.getStartPosition() ) != null) && ( this.getBoard().getPiece( move.getStartPosition() ).getTeamColor() != this.getTeamTurn())){
            throw new InvalidMoveException(String.format("%s at %s belongs to the wrong team.  It is %s turn", this.getBoard().getPiece( move.getStartPosition() ).toString(), move.getStartPosition().toString(), this.getTeamTurn().toString()));
        }
        // check that any piece at the end position does actually belong to the enemy color
        if (( this.getBoard().getPiece( move.getEndPosition() ) != null) && ( this.getBoard().getPiece( move.getEndPosition() ).getTeamColor() == this.getTeamTurn())) {
            throw new InvalidMoveException(String.format("You cannot capture your own piece at %s", move.getEndPosition().toString()));
        }

        Collection<ChessMove> possibleMoves = this.getBoard().getPiece( move.getStartPosition() ).pieceMoves( this.getBoard(), move.getStartPosition());
        if (! possibleMoves.contains( move ) ){
            throw new InvalidMoveException(String.format("%s is not a valid move for a %s", move.toString(), this.getBoard().getPiece( move.getStartPosition() ).toString()));
        }
        // does not place its own team into check
        // (or otherwise does not leave its own team in check)

        ChessGame gameCopy = new ChessGame( this);
        // make the move on the copy
        ChessPiece.PieceType pieceType;
        if (move.getPromotionPiece() != null){
            pieceType = move.getPromotionPiece();
        } else {
            pieceType = gameCopy.getBoard().getPiece( move.getStartPosition() ).getPieceType();
        }
        // move the piece
        gameCopy.board.addPiece( move.getEndPosition(), new ChessPiece( gameCopy.getTeamTurn(), pieceType)) ;
        // and remove the piece
        gameCopy.board.addPiece( move.getStartPosition(), null);
        Boolean check = gameCopy.isInCheck( getTeamTurn() );

        if (! check){
            // make the move on the actual gameboard
            this.board.addPiece( move.getEndPosition(), new ChessPiece( this.getTeamTurn(), pieceType));
            // and remove the piece from the starting square
            this.board.addPiece( move.getStartPosition(), null);
            // and record that the next team is up
            this.setTeamTurn( this.getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK: TeamColor.WHITE );
        } else {
            throw new InvalidMoveException(String.format("Move %s would leave your team in check!", move.toString()));
        }

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // a team is in check if its king lies in the path of travel of any of the other team's pieces
        ArrayList<ChessPiece> enemyPieces = new ArrayList<ChessPiece>();

        // find your team's king on the board
        ChessPiece comparisonKing = new ChessPiece( teamColor, ChessPiece.PieceType.KING);  // for later comparison
        ChessPosition myKingPosition = null;
        for (int row=1; row<=8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition square = new ChessPosition(row, col);
                if ((this.getBoard().getPiece(square) != null) && (this.getBoard().getPiece(square).equals(comparisonKing))) {
                    myKingPosition = square;
                }
            }
        }
        // now look at each enemy piece's possible moves.  Can any attack your King?
        for (int row=1; row<=8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition square = new ChessPosition(row, col);
                if ((this.getBoard().getPiece( square ) != null) && (this.getBoard().getPiece( square ).getTeamColor() != teamColor))  {
                    // then there is an enemy piece in the square
                    // and we should check to see if it can attack the king
                    ChessPiece enemyPiece = this.getBoard().getPiece( square );
                    ArrayList<ChessMove> enemyMoves = (ArrayList<ChessMove>) enemyPiece.pieceMoves( this.getBoard(), square );
                    for (ChessMove move : enemyMoves){
                        // check to see if enemyPiece can get to myKingPosition
                        if (move.getEndPosition().equals( myKingPosition )){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
