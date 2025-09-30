package chess;

import java.util.ArrayList;

public class PieceMovesCalculator {
    private ChessBoard board;
    private ChessPosition position;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }

    public ArrayList<ChessMove> calculateBishopMoves(){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        // search up and right
        for (int row= position.getRow()+1; row<=8; row++){
            if ((row < 1) || (row >8)){
                continue;
            }
            int col = position.getColumn() + ( row - position.getRow());
            if ((col < 1) || (col >8)){
                break;
            }
            ChessPosition newPosition = new ChessPosition( row, col );

            if ((board.getPiece( newPosition ) != null)  && (board.getPiece( newPosition).getTeamColor() != board.getPiece( position ).getTeamColor())){
                // enemy piece, can capture
                possibleMoves.add(new ChessMove( position, newPosition, null));
                break;
            }
            if ((board.getPiece( newPosition ) != null) && (board.getPiece( newPosition).getTeamColor() == board.getPiece( position ).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove( position, newPosition, null));
        }

        // search up and left
        for (int row= position.getRow()+1; row<=8; row++){
            if ((row < 1) || (row >8)){
                continue;
            }
            int col = position.getColumn() - ( row - position.getRow());
            if ((col < 1) || (col >8)){
                break;
            }
            ChessPosition newPosition = new ChessPosition( row, col );

            if ((board.getPiece( newPosition ) != null)  && (board.getPiece( newPosition).getTeamColor() != board.getPiece( position ).getTeamColor())){
                // enemy piece, can capture
                possibleMoves.add(new ChessMove( position, newPosition, null));
                break;
            }
            if ((board.getPiece( newPosition ) != null) && (board.getPiece( newPosition).getTeamColor() == board.getPiece( position ).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove( position, newPosition, null));
        }

        // now search down and left
        for (int row= position.getRow()-1; row>=1; row--){
            if ((row < 1) || (row >8)){
                continue;
            }
            int col = position.getColumn() - ( position.getRow() - row);
            if ((col < 1) || (col >8)){
                break;
            }
            ChessPosition newPosition = new ChessPosition( row, col );

            if ((board.getPiece( newPosition ) != null)  && (board.getPiece( newPosition).getTeamColor() != board.getPiece( position ).getTeamColor())){
                // enemy piece, can capture
                possibleMoves.add(new ChessMove( position, newPosition, null));
                break;
            }
            if ((board.getPiece( newPosition ) != null) && (board.getPiece( newPosition).getTeamColor() == board.getPiece( position ).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove( position, newPosition, null));
        }

        // now search down and right
        for (int row= position.getRow()-1; row>=1; row--){
            if ((row < 1) || (row >8)){
                continue;
            }
            int col = position.getColumn() + ( position.getRow() - row);
            if ((col < 1) || (col >8)){
                break;
            }
            ChessPosition newPosition = new ChessPosition( row, col );

            if ((board.getPiece( newPosition ) != null)  && (board.getPiece( newPosition).getTeamColor() != board.getPiece( position ).getTeamColor())){
                // enemy piece, can capture
                possibleMoves.add(new ChessMove( position, newPosition, null));
                break;
            }
            if ((board.getPiece( newPosition ) != null) && (board.getPiece( newPosition).getTeamColor() == board.getPiece( position ).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove( position, newPosition, null));
        }
        return possibleMoves;
    }

    public ArrayList<ChessMove> calculatePerpendicularMoves() {
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        ArrayList<ChessPosition> piecesOnPerp = new ArrayList<>();

        // search above
        for (int row = position.getRow() + 1; row <= 8; row++) {
            if ((row < 1) && (row > 8)) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(row, position.getColumn());
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() != board.getPiece(position).getTeamColor())) {
                // enemy, can capture but not pass
                possibleMoves.add(new ChessMove(position, newPosition, null));
                break;
            }
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() == board.getPiece(position).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove(position, newPosition, null));
        }
        // search below
        for (int row = position.getRow() - 1; row >= 1; row--) {
            if ((row < 1) && (row > 8)) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(row, position.getColumn());
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() != board.getPiece(position).getTeamColor())) {
                // enemy, can capture but not pass
                possibleMoves.add(new ChessMove(position, newPosition, null));
                break;
            }
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() == board.getPiece(position).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove(position, newPosition, null));
        }
        // search left
        for (int col = position.getColumn() - 1; col >= 1; col--) {
            if ((col < 1) && (col > 8)) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(position.getRow(), col);
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() != board.getPiece(position).getTeamColor())) {
                // enemy, can capture but not pass
                possibleMoves.add(new ChessMove(position, newPosition, null));
                break;
            }
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() == board.getPiece(position).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove(position, newPosition, null));
        }
        // search right
        for (int col = position.getColumn() + 1; col <= 8; col++) {
            if ((col < 1) && (col > 8)) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(position.getRow(), col);
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() != board.getPiece(position).getTeamColor())) {
                // enemy, can capture but not pass
                possibleMoves.add(new ChessMove(position, newPosition, null));
                break;
            }
            if ((board.getPiece(newPosition) != null) && (board.getPiece(newPosition).getTeamColor() == board.getPiece(position).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove(position, newPosition, null));
        }
        return possibleMoves;
    }

    public ArrayList<ChessMove> calculateQueenMoves(){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        ArrayList<ChessMove> diagMoves = new ArrayList<ChessMove>();
        ArrayList<ChessMove> perpMoves = new ArrayList<ChessMove>();

        diagMoves = calculateBishopMoves();
        perpMoves = calculatePerpendicularMoves();
        boolean combined = diagMoves.addAll(perpMoves);
        return diagMoves;
    }

    public ArrayList<ChessMove> calculateKingMoves(){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        possibleMoves = calculateQueenMoves();
        possibleMoves.removeIf(m->(Math.abs(m.getEndPosition().getRow() - position.getRow()) > 1) || (Math.abs(m.getEndPosition().getColumn() - position.getColumn() )> 1));
        return possibleMoves;
    }

    public ArrayList<ChessMove> calculatePawnMoves(){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        ChessPiece.PieceType[] promotionPieces = new ChessPiece.PieceType[4];
        boolean blockedAtOne = new Boolean(false);
        boolean blockedAtTwo = new Boolean(false);

        // you can promote pawns to QUEEN, ROOK, BISHOP, or KNIGHT (who knew?)
        promotionPieces[0] = ChessPiece.PieceType.BISHOP;
        promotionPieces[1] = ChessPiece.PieceType.KNIGHT;
        promotionPieces[2] = ChessPiece.PieceType.QUEEN;
        promotionPieces[3] = ChessPiece.PieceType.ROOK;

        if ( board.getPiece( position ).getTeamColor() == ChessGame.TeamColor.WHITE){
            // by convention (see PawnMoveTests), WHITE begins on rows 1 and 2
            // check if at edge and if blocked
            ChessPosition oneSpace = new ChessPosition( position.getRow() + 1, position.getColumn() );
            if ((oneSpace.getRow() <= 8) && (board.getPiece(oneSpace) != null)) {
                blockedAtOne = true;
            }
            ChessPosition twoSpaces = new ChessPosition( position.getRow() + 2, position.getColumn() );
            if ((twoSpaces.getRow() <= 8) && (board.getPiece(twoSpaces) != null)){
                blockedAtTwo = true;
            }
            if ((position.getRow() == 2) && (!blockedAtOne) && !(blockedAtTwo)){
                // can optionally move two spaces forward
                possibleMoves.add( new ChessMove( position, new ChessPosition( position.getRow() + 2, position.getColumn()), null));
            }
            // can optionally move one space forward
            // if NOT blocked and NOT at edge
            if ((!blockedAtOne) && (position.getRow() + 1 <= 8)){
                possibleMoves.add(new ChessMove(position, new ChessPosition(position.getRow() + 1, position.getColumn()), null));
            }
            // can capture diagonally
            if ((position.getRow() + 1 <= 8) && (position.getColumn() - 1 >= 1) ){
                ChessPosition upperLeft = new ChessPosition( position.getRow() + 1, position.getColumn() - 1);
                // IE if one space up and left exists on chess board
                if ((board.getPiece(upperLeft) != null) && (board.getPiece( upperLeft ).getTeamColor() == ChessGame.TeamColor.BLACK )){
                    // enemy piece, can capture
                    possibleMoves.add( new ChessMove(position, upperLeft, null ));
                }
                // else if a friendly piece is at upperLeft or if empty, do nothing
            }
            if ((position.getRow() + 1 <= 8) && (position.getColumn() + 1 <= 8) ){
                ChessPosition upperRight = new ChessPosition( position.getRow() + 1, position.getColumn() + 1);
                // IE if one space up and left exists on the chess board
                if ((board.getPiece(upperRight) != null) && (board.getPiece( upperRight ).getTeamColor() == ChessGame.TeamColor.BLACK )){
                    // enemy piece, can capture
                    possibleMoves.add( new ChessMove( position, upperRight, null) );
                }
                // else if a friendly piece is at upperRight or if empty, do nothing
            }
        }
        if ( board.getPiece( position ).getTeamColor() == ChessGame.TeamColor.BLACK ){
            // by convention (see PawnMoveTests), BLACK begins on rows 8 and 7
            // check if at edge and if blocked
            ChessPosition oneSpace = new ChessPosition( position.getRow() - 1, position.getColumn());
            if ((oneSpace.getRow() >= 1 ) && (board.getPiece( oneSpace ) != null)) {
                blockedAtOne = true;
            }
            ChessPosition twoSpaces = new ChessPosition( position.getRow() - 2, position.getColumn());
            if ((twoSpaces.getRow() >= 1) && (board.getPiece(twoSpaces) != null)){
                blockedAtTwo = true;
            }
            if ((position.getRow() == 7 ) && (!blockedAtOne) && (!blockedAtTwo)){
                // can optionally move two spaces forward
                possibleMoves.add( new ChessMove( position, new ChessPosition( position.getRow() - 2, position.getColumn()), null));
            }
            // can optionally move one space forward
            // if NOT blocked and NOT at edge
            if ((!blockedAtOne) && (position.getRow() - 1 >= 1)){
            possibleMoves.add( new ChessMove( position, new ChessPosition( position.getRow() - 1, position.getColumn()), null));
            }
            // can capture diagonally
            if ((position.getRow() - 1 >= 1) && (position.getColumn() - 1 >= 1)){
                ChessPosition lowerLeft = new ChessPosition(position.getRow() - 1, position.getColumn() - 1);
                // IE if one space down and left exists on the chess board
                if ((board.getPiece(lowerLeft) != null) && (board.getPiece( lowerLeft ).getTeamColor() == ChessGame.TeamColor.WHITE)){
                    // enemy piece, can capture
                    possibleMoves.add( new ChessMove( position, lowerLeft, null));
                }
                // else if a friendly piece is at lowerLeft or if empty, do nothing;
            }
            if ((position.getRow() - 1 >= 1) && (position.getColumn() + 1 <= 8)){
                ChessPosition lowerRight = new ChessPosition(position.getRow() -1, position.getColumn() + 1);
                // IE if one space down and right exists on the chess board
                if ((board.getPiece(lowerRight) != null) && (board.getPiece( lowerRight ).getTeamColor() == ChessGame.TeamColor.WHITE)){
                    // enemy piece, can capture
                    possibleMoves.add( new ChessMove( position, lowerRight, null));
                }
                // else if a friendly piece is at lowerRight or if empty, do nothing
            }
        }
        // now make sure to duplicate moves that will result in a promotion
        // move (startPosition, endPosition, QUEEN) != (startPosition, endPosition, ROOK), e.g.
        ArrayList<ChessMove> postPromoMoves = new ArrayList<ChessMove>();
        for (ChessMove move: possibleMoves){
            if ((move.getEndPosition().getRow()==8) || (move.getEndPosition().getRow()==1)){
                for (ChessPiece.PieceType promoPiece : promotionPieces){
                    postPromoMoves.add( new ChessMove( move.getStartPosition(), move.getEndPosition(), promoPiece));
                }
            } else {
                // just copy the non-promoting move over once
                postPromoMoves.add( move );
            }
        }
        return postPromoMoves;
    }

    public ArrayList<ChessMove> calculateKnightMoves(){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        ChessPosition upLeft = new ChessPosition( position.getRow() + 2, position.getColumn() - 1);
        ChessPosition leftUp = new ChessPosition( position.getRow() + 1, position.getColumn() - 2);
        ChessPosition leftDown = new ChessPosition( position.getRow() - 1, position.getColumn() - 2 );
        ChessPosition downLeft = new ChessPosition( position.getRow() - 2, position.getColumn() - 1);
        ChessPosition downRight = new ChessPosition( position.getRow() - 2, position.getColumn() + 1);
        ChessPosition rightDown = new ChessPosition( position.getRow() - 1 , position.getColumn() + 2);
        ChessPosition rightUp = new ChessPosition( position.getRow() + 1, position.getColumn() + 2);
        ChessPosition upRight = new ChessPosition( position.getRow() + 2, position.getColumn() + 1);

        possibleMoves.add( new ChessMove(position, upLeft, null));
        possibleMoves.add( new ChessMove(position, leftUp, null));
        possibleMoves.add( new ChessMove(position, leftDown, null));
        possibleMoves.add( new ChessMove(position, downLeft, null));
        possibleMoves.add( new ChessMove(position, downRight, null));
        possibleMoves.add( new ChessMove(position, rightDown, null));
        possibleMoves.add( new ChessMove(position, rightUp, null));
        possibleMoves.add( new ChessMove(position, upRight, null));

        // check that end positions are not out of bounds
        possibleMoves.removeIf(m->m.getEndPosition().getRow() < 1);
        possibleMoves.removeIf(m->m.getEndPosition().getRow() > 8);
        possibleMoves.removeIf(m->m.getEndPosition().getColumn() < 1);
        possibleMoves.removeIf(m->m.getEndPosition().getColumn() > 8);

        // check that end positions are not occupied by a friendly piece
        possibleMoves.removeIf(m->((board.getPiece(m.getEndPosition()) != null) && (board.getPiece(m.getEndPosition()).getTeamColor() == board.getPiece( position ).getTeamColor())));

        return possibleMoves;


    }

}
