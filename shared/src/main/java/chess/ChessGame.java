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
    private boolean blackKingSideRookMoved;
    private boolean blackQueenSideRookMoved;
    private boolean blackKingMoved;
    private boolean whiteKingSideRookMoved;
    private boolean whiteQueenSideRookMoved;
    private boolean whiteKingMoved;
    private boolean investigatingCastle;


    public ChessGame() {
        this.whoseTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.prevMove = null;
        this.blackKingSideRookMoved = false;
        this.blackQueenSideRookMoved = false;
        this.blackKingMoved = false;
        this.whiteKingSideRookMoved = false;
        this.whiteQueenSideRookMoved = false;
        this.whiteKingMoved = false;
        this.investigatingCastle = false;

    }

    public ChessGame(ChessGame original){
        this.whoseTurn = original.getTeamTurn();
        this.board = new ChessBoard( original.getBoard() );
        this.prevMove = original.getPrevMove();
        this.blackKingSideRookMoved = original.blackKingSideRookMoved;
        this.blackQueenSideRookMoved = original.blackQueenSideRookMoved;
        this.blackKingMoved = original.blackKingMoved;
        this.whiteKingSideRookMoved = original.whiteKingSideRookMoved;
        this.whiteQueenSideRookMoved = original.whiteQueenSideRookMoved;
        this.whiteKingMoved = original.whiteKingMoved;
        this.investigatingCastle = true;
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
            if (this.canKingSideCastle( startPosition )){
                ChessMove kingSideCastle = formKingSideCastleMove( startPosition );
                validMoves.add( kingSideCastle );
            }
            if (this.canQueenSideCastle( startPosition )){
                ChessMove queenSideCastle = formQueenSideCastleMove( startPosition );
                validMoves.add( queenSideCastle );
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
        if ((!this.investigatingCastle) && (this.canKingSideCastle( move.getStartPosition() ))){
            ChessMove kingSideCastle = formKingSideCastleMove( move.getStartPosition() );
            if (kingSideCastle != null){
                possibleMoves.add(kingSideCastle);
            }
        }
       if ((!this.investigatingCastle) && (this.canQueenSideCastle( move.getStartPosition()))){
           ChessMove queenSideCastle = formQueenSideCastleMove( move.getStartPosition() );
           if (queenSideCastle != null){
               possibleMoves.add(queenSideCastle);
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
            // record king movement record (for castling)
            if ( this.getBoard().getPiece( move.getStartPosition() ).getPieceType() == ChessPiece.PieceType.KING ){
                if (this.getTeamTurn() == TeamColor.WHITE){
                    this.whiteKingMoved = true;
                }
                if (this.getTeamTurn() == TeamColor.BLACK){
                    this.blackKingMoved = true;
                }
                // and record if king-side castling
                if ((move.getStartPosition().getColumn()==5) && (move.getEndPosition().getColumn() == 7)){
                    // move the rook
                    this.getBoard().addPiece( new ChessPosition( move.getStartPosition().getRow(), 6), new ChessPiece( this.getTeamTurn(), ChessPiece.PieceType.ROOK));
                    // and remove the rook
                    this.getBoard().addPiece( new ChessPosition(move.getStartPosition().getRow(), 8), null);
                }
                // and record if queen-side castling
                if ((move.getStartPosition().getColumn()==5) && (move.getEndPosition().getColumn() == 3)){
                    // move the rook
                    this.getBoard().addPiece( new ChessPosition( move.getStartPosition().getRow(), 4), new ChessPiece( this.getTeamTurn(), ChessPiece.PieceType.ROOK));
                    // and remove the rook
                    this.getBoard().addPiece( new ChessPosition( move.getStartPosition().getRow(), 1), null);
                }
            }
            // record rook movement record (for castling)
            if ( this.getBoard().getPiece( move.getStartPosition() ).getPieceType() == ChessPiece.PieceType.ROOK){
                if (this.getTeamTurn() == TeamColor.WHITE){
                    if ((move.getStartPosition().getRow() == 1) && (move.getStartPosition().getColumn() == 1)){
                        this.whiteQueenSideRookMoved = true;
                    }
                    if ((move.getStartPosition().getRow() == 1) && (move.getStartPosition().getColumn() == 8)){
                        this.whiteKingSideRookMoved = true;
                    }
                }
                if (this.getTeamTurn() == TeamColor.BLACK){
                    if ((move.getStartPosition().getRow() == 8) && (move.getStartPosition().getColumn() == 1)){
                        this.blackQueenSideRookMoved = true;
                    }
                    if ((move.getStartPosition().getRow() == 8) && (move.getStartPosition().getColumn() == 8)){
                        this.blackKingSideRookMoved = true;
                    }
                }
            }
            // make the move on the actual gameboard
            this.getBoard().addPiece( move.getEndPosition(), new ChessPiece( this.getTeamTurn(), pieceType));
            // and remove the piece from the starting square
            this.getBoard().addPiece( move.getStartPosition(), null);
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

    private ChessMove formKingSideCastleMove( ChessPosition kingStartPosition ){
        ChessPosition endKingPosition;
        if ((! canKingSideCastle( kingStartPosition) )){
            return null;
        }
        // then you should note that king side castle is possible
        if (this.getBoard().getPiece( kingStartPosition ).getTeamColor() == TeamColor.BLACK){
            endKingPosition = new ChessPosition(8, 7);
        } else {
            endKingPosition = new ChessPosition(1, 7);
        }
            return new ChessMove(kingStartPosition, endKingPosition, null);
    }

    private ChessMove formQueenSideCastleMove( ChessPosition kingStartPosition ){
        ChessPosition endKingPosition;
        if ((! canQueenSideCastle( kingStartPosition) )){
            return null;
        }
        // then you should note that king side castle is possible
        if (this.getBoard().getPiece( kingStartPosition ).getTeamColor() == TeamColor.BLACK){
            endKingPosition = new ChessPosition(8, 3);
        } else {
            endKingPosition = new ChessPosition(1, 3);
        }
        return new ChessMove(kingStartPosition, endKingPosition, null);
    }

    private boolean canKingSideCastle( ChessPosition kingStartPosition ) {
        if (this.getBoard().getPiece( kingStartPosition ).getPieceType() != ChessPiece.PieceType.KING){
            return false;
        }
        ChessPosition bishopSquare = new ChessPosition(8, 6);
        ChessPosition knightSquare = new ChessPosition(8, 7);
        ChessPosition rookSquare = new ChessPosition(8, 8);

        if (this.getBoard().getPiece( kingStartPosition ).getTeamColor() == TeamColor.WHITE) {
            bishopSquare = new ChessPosition(1, 6);
            knightSquare = new ChessPosition(1, 7);
            rookSquare = new ChessPosition(1, 8);
            if (this.whiteKingMoved || this.whiteKingSideRookMoved || !kingStartPosition.equals( new ChessPosition( 1, 5))) {
                return false;
            }
        } else {
            if (this.blackKingMoved || this.blackKingSideRookMoved || !kingStartPosition.equals( new ChessPosition( 8, 5))){
                return false;
            }
        }
        if ((this.getBoard().getPiece(bishopSquare)==null) && (this.getBoard().getPiece(knightSquare)==null)){
            // you can move... if not in check at any position!
            if (! this.isInCheck( this.getTeamTurn() )){
                ChessGame movedKing = new ChessGame( this );
                // fixme should I catch these invalid move exceptions and treat them differently here?
                try{
                    movedKing.makeMove( new ChessMove( kingStartPosition, bishopSquare, null));
                } catch (InvalidMoveException e){
                    return false;
                }
                if ( movedKing.isInCheck( this.getTeamTurn() )){
                    return false;
                }
                movedKing.setTeamTurn( this.getTeamTurn() != TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE );  // moving king one square at a time to ensure no check in path
                try{
                    movedKing.makeMove( new ChessMove( bishopSquare, knightSquare, null));
                } catch (InvalidMoveException e){
                    return false;
                }
                if ( movedKing.isInCheck( this.getTeamTurn()) ){
                    return false;
                }
                // now try setting the rook
                movedKing.getBoard().addPiece( bishopSquare, new ChessPiece( this.getTeamTurn(), ChessPiece.PieceType.ROOK));
                // and remove the rook
                movedKing.getBoard().addPiece( rookSquare, null);
                if (movedKing.isInCheck( this.getTeamTurn())){
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean canQueenSideCastle( ChessPosition kingStartPosition ){
        if (this.getBoard().getPiece( kingStartPosition ).getPieceType() != ChessPiece.PieceType.KING){
            return false;
        }
        ChessPosition queenSquare = new ChessPosition(8, 4);
        ChessPosition bishopSquare = new ChessPosition(8, 3);
        ChessPosition knightSquare = new ChessPosition(8, 2);
        ChessPosition rookSquare = new ChessPosition(8, 1);

        if (this.getBoard().getPiece( kingStartPosition ).getTeamColor() == TeamColor.WHITE) {
            queenSquare = new ChessPosition(1, 4);
            bishopSquare = new ChessPosition(1, 3);
            knightSquare = new ChessPosition(1, 2);
            rookSquare = new ChessPosition(1, 1);
            if (this.whiteKingMoved || this.whiteQueenSideRookMoved || !kingStartPosition.equals( new ChessPosition( 1, 5))) {
                return false;
            }
        } else {
            if (this.blackKingMoved || this.blackQueenSideRookMoved || !kingStartPosition.equals( new ChessPosition( 8, 5))){
                return false;
            }
        }
        if ((this.getBoard().getPiece(queenSquare)==null) && (this.getBoard().getPiece(bishopSquare)==null) && (this.getBoard().getPiece(knightSquare)==null)){
            // you can move... if not in check at any position!
            if (! this.isInCheck( this.getTeamTurn() )){
                ChessGame movedKing = new ChessGame( this );
                // fixme should I catch these invalid move exceptions and treat them differently here?
                try{
                    movedKing.makeMove( new ChessMove( kingStartPosition, queenSquare, null));
                } catch (InvalidMoveException e){
                    return false;
                }
                if ( movedKing.isInCheck( this.getTeamTurn() )){
                    return false;
                }
                movedKing.setTeamTurn( this.getTeamTurn() != TeamColor.WHITE ? TeamColor.BLACK: TeamColor.WHITE );  // moving king one square at a time to ensure no check in path
                try{
                    movedKing.makeMove( new ChessMove( queenSquare, bishopSquare, null));
                } catch (InvalidMoveException e){
                    return false;
                }
                if ( movedKing.isInCheck( this.getTeamTurn()) ){
                    return false;
                }
                // now try setting the rook
                movedKing.getBoard().addPiece( queenSquare, new ChessPiece( this.getTeamTurn(), ChessPiece.PieceType.ROOK));
                // and remove the rook
                movedKing.getBoard().addPiece( rookSquare, null);
                if (movedKing.isInCheck( this.getTeamTurn())){
                    return false;
                }
                return true;
            }
        }
        return false;
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

//    public ChessMove enPassant(ChessPosition){
//        // assumes that chess position contains a pawn
//
//    }

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
