package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor whoseTurn;
    private ChessBoard board;
    private ChessMove prevMove;

    public ChessGame() {
        this.whoseTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.prevMove = null;

    }

    public ChessGame(ChessGame original){
        this.whoseTurn = original.getTeamTurn();
        this.board = new ChessBoard( original.getBoard() );
        this.prevMove = original.getPrevMove();
        this.prevMove = original.getPrevMove();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return whoseTurn == chessGame.whoseTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(whoseTurn, board);
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
        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();

        if (this.getBoard().getPiece( startPosition ) == null){
            // IE there is no piece at this square
            return null;
        }

        if (this.getBoard().getPiece( startPosition ) != null){
            ArrayList<ChessMove> possibleMoves = (ArrayList<ChessMove>) this.getBoard().getPiece( startPosition ).pieceMoves( this.getBoard(), startPosition);
            for (ChessMove move: possibleMoves){
                // see if the move would place the king in danger of check
                if (willMoveResultInCheck( this.getBoard(), move)){
                    continue;
                }
                else {
                    validMoves.add(move);
                }
            }

            if (isEnPassantPossible( startPosition )){
                ChessMove enPassant = formEnPassantMove( startPosition );
                validMoves.add( enPassant );
            }
            return validMoves;
        }
        return null;
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
        if (this.isEnPassantPossible( move.getStartPosition() )) {
            ChessMove enPassant = formEnPassantMove(move.getStartPosition());
            if (enPassant != null) {
                possibleMoves.add(enPassant);
            }
        }

        if (! possibleMoves.contains( move ) ){
            throw new InvalidMoveException(String.format("%s is not a valid move for a %s", move.toString(), this.getBoard().getPiece( move.getStartPosition() ).toString()));
        }
        // does not place its own team into check
        // (or otherwise does not leave its own team in check)
        ChessPiece.PieceType pieceType;
        if (move.getPromotionPiece() != null){
            pieceType = move.getPromotionPiece();
        } else {
            pieceType = this.getBoard().getPiece( move.getStartPosition() ).getPieceType();
        }
        Boolean check = willMoveResultInCheck( this.getBoard(), move);

        if (! check){
            if (isEnPassantPossible( move.getStartPosition() )){
                this.getBoard().addPiece( this.getPrevMove().getEndPosition(), null);
            }
            // make the move on the actual gameboard
            this.board.addPiece( move.getEndPosition(), new ChessPiece( this.getTeamTurn(), pieceType));
            // and remove the piece from the starting square
            this.board.addPiece( move.getStartPosition(), null);
            // and record that the next team is up
            this.setTeamTurn( this.getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK: TeamColor.WHITE );
            // if you made an enpassant move, then the now extra copy of the captured pawn must be removed
            // and record the previous move
            this.setPrevMove( move );
        } else {
            throw new InvalidMoveException(String.format("Move %s would leave your team in check!", move.toString()));
        }

    }

    private boolean willMoveResultInCheck( ChessBoard myBoard, ChessMove myMove){
        ChessGame gameCopy = new ChessGame( this );
        ChessPiece.PieceType pieceType;
        TeamColor pieceColor = gameCopy.getTeamTurn();
        if (myMove.getPromotionPiece() != null){
            pieceType = myMove.getPromotionPiece();
        } else {
            pieceType = gameCopy.getBoard().getPiece( myMove.getStartPosition() ).getPieceType();
            // necessary since you may want to test moves when it is the other team's turn
            pieceColor = gameCopy.getBoard().getPiece( myMove.getStartPosition() ).getTeamColor();
        }

        // move the piece on the copy
        gameCopy.getBoard().addPiece( myMove.getEndPosition(), new ChessPiece(pieceColor, pieceType));
        // and remove the copy of the piece that just moved
        gameCopy.getBoard().addPiece( myMove.getStartPosition(), null);

        return gameCopy.isInCheck( pieceColor );
    }

    private boolean isEnPassantPossible( ChessPosition startPosition ) {

        // check for en passant for pawns
        if ((this.getPrevMove() != null) && (this.getBoard().getPiece(startPosition) != null) && (this.getBoard().getPiece(startPosition).getPieceType() == ChessPiece.PieceType.PAWN)) {
            // if a pawn
            ChessPosition prevPawnStartPosition = this.getPrevMove().getStartPosition();
            ChessPosition prevPawnEndPosition = this.getPrevMove().getEndPosition();
            if ((this.getBoard().getPiece(prevPawnEndPosition) != null) && (this.getBoard().getPiece(prevPawnEndPosition).getPieceType() == ChessPiece.PieceType.PAWN)) {
                // now check if this previously moved pawn jumped over the current pawn's capture square
                if (Math.abs(prevPawnEndPosition.getRow() - prevPawnStartPosition.getRow()) == 2) {
                    // then the previous pawn did move two spaces... now check if adjacent to your pawn at startPosition
                    if ((Math.abs(startPosition.getColumn() - prevPawnEndPosition.getColumn()) == 1) && (startPosition.getRow() - prevPawnEndPosition.getRow() == 0)) {
                        // then you should note that enpassant capture is possible this move!
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ChessMove formEnPassantMove( ChessPosition startPosition ){
        if (! isEnPassantPossible( startPosition )){
            return null;
        }
        // then you should note that enpassant capture is possible this move!
        int rowAdvancement = 0;
        if (this.getBoard().getPiece( startPosition ).getTeamColor() == TeamColor.BLACK) {
            rowAdvancement = -1;
        } else if (this.getBoard().getPiece( startPosition ).getTeamColor() == TeamColor.WHITE) {
            rowAdvancement = +1;
        }
        ChessPosition prevPawnStartPosition = this.getPrevMove().getStartPosition();
        ChessPosition enPassantCapturePosition = new ChessPosition( startPosition.getRow() + rowAdvancement, prevPawnStartPosition.getColumn());
        ChessMove enPassant = new ChessMove(startPosition, enPassantCapturePosition, null);
        return enPassant;
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
        // in check mate if in check AND no valid moves
        if (isInCheck( teamColor )){
            // now look to see if you have any valid moves
            // start by finding your team's pieces
            for (int row=1; row<=8; row++){
                for (int col=1; col<=8; col++){
                    ChessPosition square = new ChessPosition(row, col);
                    if ((this.getBoard().getPiece( square ) != null) && (this.getBoard().getPiece( square ).getTeamColor() == teamColor)){
                        ArrayList<ChessMove> validMoves = (ArrayList<ChessMove>) this.validMoves( square );
                        if ((validMoves != null) && (validMoves.size() > 0)){
                            // null returned if there are no moves from this square
                            // so if we do find a valid move, then we are not in checkmate
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // in stalemate if not in check AND no valid moves
        if (!isInCheck( teamColor )){
            // now look to see if you have any valid moves
            // start by finding your team's pieces
            for (int row=1; row<=8; row++){
                for (int col=1; col<=8; col++){
                    ChessPosition square = new ChessPosition(row, col);
                    if ((this.getBoard().getPiece( square ) != null) && (this.getBoard().getPiece( square ).getTeamColor() == teamColor)){
                        ArrayList<ChessMove> validMoves = (ArrayList<ChessMove>) this.validMoves( square );
                        if ((validMoves != null) && (validMoves.size() > 0)){
                            // null returned if there are no moves from this square
                            // so if we do find a valid move, then we are not in stalemate
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
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


    public ChessMove getPrevMove() {
        return prevMove;
    }

    public void setPrevMove(ChessMove prevMove) {
        this.prevMove = prevMove;
    }
}
