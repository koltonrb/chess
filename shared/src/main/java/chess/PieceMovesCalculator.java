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
        ArrayList<ChessPosition> piecesOnDiagonals;

        numRows = 8;
        numCols = 8;
        diagonalMoves = new ArrayList<ChessMove>();
        piecesOnDiagonals = new ArrayList<ChessPosition>();
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
                    // IE if (row, col) is on a diagonal from the current position
                    ChessPosition nextPosition = new ChessPosition(row, col);

                    if (board.getPiece( nextPosition ) != null && board.getPiece( nextPosition ).getTeamColor() == board.getPiece( position ).getTeamColor()){
                        // friendly piece, cannot capture nor pass
                        piecesOnDiagonals.add( nextPosition );
                        continue;  // keep searching columns across the row
                    }

                    diagonalMoves.add(new ChessMove( position, nextPosition,  null));

                    if (board.getPiece( nextPosition ) != null && board.getPiece( nextPosition ).getTeamColor() != board.getPiece( position ).getTeamColor()) {
                        // enemy piece, can capture but cannot pass
                        piecesOnDiagonals.add( nextPosition );
                        continue; // keep searching columns across the row
                    }
                }

            }
        }
        // remove spaces that are on the diagonal but which would pass over an occupied square
        for (ChessPosition limit:piecesOnDiagonals){
            // up and to the right of position
            if ((position.getRow() - limit.getRow() < 0) && (position.getColumn() - limit.getColumn() < 0)) {
                diagonalMoves.removeIf( p -> p.getEndPosition().getRow() > limit.getRow() && p.getEndPosition().getColumn() > limit.getColumn() );
            }
            // up and to the left
            if ((position.getRow() - limit.getRow() < 0) && (position.getColumn() - limit.getColumn() > 0 )) {
                diagonalMoves.removeIf( p -> p.getEndPosition().getRow() > limit.getRow() && p.getEndPosition().getColumn() < limit.getColumn() );
            }
            // down and to the left
            if ((position.getRow() - limit.getRow() > 0) && (position.getColumn() - limit.getColumn() > 0)) {
                diagonalMoves.removeIf(p -> p.getEndPosition().getRow() < limit.getRow() && p.getEndPosition().getColumn() < limit.getColumn());
            }
            // down and to the right
            if ((position.getRow() - limit.getRow() > 0) && (position.getColumn() - limit.getColumn() < 0)) {
                diagonalMoves.removeIf(p->p.getEndPosition().getRow() < limit.getRow() && p.getEndPosition().getColumn() > limit.getColumn());
            }
        }
        return diagonalMoves;
    }

    public ArrayList<ChessMove> calculatePerpendicularMoves(ChessBoard board, ChessPosition position) {

        int numRows;
        int numCols;
        int[] rows;
        int [] cols;
        ArrayList<ChessMove>  perpendicularMoves;
        ArrayList<ChessPosition> piecesOnPerpendicular;

        numRows = 8;
        numCols = 8;
        perpendicularMoves = new ArrayList<ChessMove>();
        piecesOnPerpendicular = new ArrayList<ChessPosition>();
        rows = fillGridOptions(1, numRows);
        cols = fillGridOptions(1, numCols);

        // make a list of ordered pairs (row, col) to consider for perpendicular moves
        for (int i = 0; i < numRows; i++) {
            // consider moves for every row on column position.getColumn()
            int row = rows[i];
            if (row == position.getRow()) {
                continue; //the current position cannot be a move; we will consider moving along this row next
            }
            ChessPosition nextPosition = new ChessPosition(row, position.getColumn());
            if (board.getPiece(nextPosition) != null && board.getPiece(nextPosition).getTeamColor() == board.getPiece(position).getTeamColor()) {
                // friendly piece, cannot capture nor pass
                piecesOnPerpendicular.add(nextPosition);
                continue;  // keep searching the other rows across the column
            }

            perpendicularMoves.add(new ChessMove(position, nextPosition, null));

            if (board.getPiece(nextPosition) != null && board.getPiece(nextPosition).getTeamColor() != board.getPiece(position).getTeamColor()) {
                // enemy piece, can capture but cannot pass
                piecesOnPerpendicular.add(nextPosition);
                continue; // keep searching rows across the column
            }
        }

        for ( int j = 0; j < numRows; j++){
            // consider moves for every row on column position.getRow()
            int col = cols[j];
            if (col == position.getColumn()){
                continue; // current position cannot be a move; we considered moving along this column above
            }
            ChessPosition nextPosition = new ChessPosition(position.getRow(), col);
            if ((board.getPiece( nextPosition ) != null) && (board.getPiece( nextPosition ).getTeamColor() == board.getPiece( position ).getTeamColor())){
                // friendly piece, cannot capture nor pass
                piecesOnPerpendicular.add( nextPosition );
                continue;  // keep searching the other columns across the row
            }
            perpendicularMoves.add(new ChessMove( position, nextPosition, null));

            if ((board.getPiece( nextPosition) != null) && (board.getPiece( nextPosition ).getTeamColor() != board.getPiece( position ).getTeamColor())){
                // enemy piece, can capture but cannot pass
                piecesOnPerpendicular.add( nextPosition );
                continue; //keep searching columns across the row
            }

        }
        // remove spaces that are on the perpendicular but which would pass over an occupied square
        for (ChessPosition limit:piecesOnPerpendicular) {
            // limit above position
            if ((position.getColumn() == limit.getColumn()) && (position.getRow() < limit.getRow())) {
                perpendicularMoves.removeIf(p -> p.getEndPosition().getRow() > limit.getRow());
            }
            // limit below position
            if ((position.getColumn() == limit.getColumn()) && (position.getRow() > limit.getRow())) {
                perpendicularMoves.removeIf(p -> p.getEndPosition().getRow() < limit.getRow());
            }
            // limit left of position
            if ((position.getRow() == limit.getRow()) && (position.getColumn() > limit.getColumn())) {
                perpendicularMoves.removeIf(p -> p.getEndPosition().getColumn() < limit.getColumn());
            }
            // limit right of position
            if ((position.getRow() == limit.getRow()) && (position.getColumn() < limit.getColumn())) {
                perpendicularMoves.removeIf(p -> p.getEndPosition().getColumn() > limit.getColumn());
            }
        }
        return perpendicularMoves;
    }

    /*
    fillGridOptions returns an array of possible integer values representing either the vertical
    or the horizontal dimension of the ChessBoard
     */
    private int[] fillGridOptions(int min, int max) {
        int[] vals;

        vals = new int[max - min + 1];

        for (int i = min; i < max + 1; i++){ // + 1 since min and max are row/col numbers, not indices
            vals[i - 1] = i;  // zero indexed array
        }
        //vals.remove(current); // we just want a list of spaces to which we might be able to move
        return vals;
    }

    public ArrayList<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition position){
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

    public ArrayList<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition position){
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
        possibleMoves.add( new ChessMove(position, downLeft, null));
        possibleMoves.add( new ChessMove(position, downRight, null));
        possibleMoves.add( new ChessMove(position, rightDown, null));
        possibleMoves.add( new ChessMove(position, rightUp, null));
        possibleMoves.add( new ChessMove(position, upRight, null));

        possibleMoves.removeIf(m->m.getEndPosition().getRow() < 1);
        possibleMoves.removeIf(m->m.getEndPosition().getRow() > 8);
        possibleMoves.removeIf(m->m.getEndPosition().getColumn() < 1);
        possibleMoves.removeIf(m->m.getEndPosition().getColumn() > 8);

        possibleMoves.removeIf(m->((board.getPiece(m.getEndPosition()) != null) && (board.getPiece(m.getEndPosition()).getTeamColor() == board.getPiece( position ).getTeamColor())));

        return possibleMoves;


    }

}
