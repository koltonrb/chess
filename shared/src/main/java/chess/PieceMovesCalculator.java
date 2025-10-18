package chess;

import java.util.ArrayList;

public class PieceMovesCalculator {
    private ChessBoard board;
    private ChessPosition position;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }

    private boolean offBoard(int rowOrCol){
        int lowerLimit = 1;
        int upperLimit = 8;
        return (rowOrCol < 1) || (rowOrCol > 8);
    }

    /**
     * Searches along a single ray until an edge of the board or until another piece is encountered.
     * returns a list of possible moves
     * and accounts for if encountered pieces are friendly or enemy
     * @param rowIncrement
     * @param colIncrement
     * @return
     */
    private ArrayList<ChessMove> calculateGridMoves(int rowIncrement, int colIncrement){
        int row; // = position.getRow();
        int col; // = position.getColumn();
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        for (int addend = 1; addend < 8; addend++) {
            row = position.getRow() + addend * rowIncrement;
            col = position.getColumn() + addend * colIncrement;
            if (offBoard( row )){
                break;
            }
            if (offBoard( col )){
                break;
            }

            ChessPosition newPosition = new ChessPosition( row, col );

            if ((board.getPiece( newPosition ) != null) && (board.getPiece( newPosition).getTeamColor() == board.getPiece( position ).getTeamColor())) {
                // friendly piece
                break;
            }
            possibleMoves.add(new ChessMove( position, newPosition, null));
            if ((board.getPiece( newPosition ) != null)  && (board.getPiece( newPosition).getTeamColor() != board.getPiece( position ).getTeamColor())){
                // enemy piece, can capture
                break;
            }
//            possibleMoves.add(new ChessMove( position, newPosition, null));
        }
        return possibleMoves;
    }

    public ArrayList<ChessMove> calculateBishopMoves() {
        ArrayList<ChessMove> possibleMovesUpRight = new ArrayList<ChessMove>();
        ArrayList<ChessMove> possibleMovesUpLeft = new ArrayList<ChessMove>();
        ArrayList<ChessMove> possibleMovesDownLeft = new ArrayList<ChessMove>();
        ArrayList<ChessMove> possibleMovesDownRight = new ArrayList<ChessMove>();

        // search up and right
        possibleMovesUpRight = calculateGridMoves(1, 1);
        possibleMovesUpLeft = calculateGridMoves(1, -1);
        possibleMovesDownLeft = calculateGridMoves(-1, -1);
        possibleMovesDownRight = calculateGridMoves(-1, 1);

        possibleMovesUpRight.addAll(possibleMovesUpLeft);
        possibleMovesUpRight.addAll(possibleMovesDownLeft);
        possibleMovesUpRight.addAll(possibleMovesDownRight);

        return possibleMovesUpRight;
    }

    public ArrayList<ChessMove> calculatePerpendicularMoves() {
        ArrayList<ChessMove> possibleMovesUp = new ArrayList<ChessMove>();
        ArrayList<ChessMove> possibleMovesLeft = new ArrayList<ChessMove>();
        ArrayList<ChessMove> possibleMovesDown = new ArrayList<ChessMove>();
        ArrayList<ChessMove> possibleMovesRight = new ArrayList<ChessMove>();

        possibleMovesUp = calculateGridMoves(1, 0);
        possibleMovesLeft = calculateGridMoves(0, -1);
        possibleMovesDown = calculateGridMoves(-1, 0);
        possibleMovesRight = calculateGridMoves(0, 1);

        possibleMovesUp.addAll(possibleMovesLeft);
        possibleMovesUp.addAll(possibleMovesDown);
        possibleMovesUp.addAll(possibleMovesRight);

        return possibleMovesUp;
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

    private ArrayList<ChessMove> findPawnMoves(ChessGame.TeamColor teamColor){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        boolean blockedAtOne = new Boolean(false);
        boolean blockedAtTwo = new Boolean(false);
        int isWhite;
        int pawnStartRow;
        ChessGame.TeamColor enemyColor;

        if (teamColor == ChessGame.TeamColor.WHITE){
            // by convention (see PawnMoveTests), WHITE begins on rows 1 and 2
            // check if at edge and if blocked
            isWhite = 1;
            pawnStartRow = 2;
            enemyColor = ChessGame.TeamColor.BLACK;

        } else{
            isWhite = -1;
            pawnStartRow = 7;
            enemyColor = ChessGame.TeamColor.WHITE;
        }
        // by convention (see PawnMoveTests), WHITE begins on rows 1 and 2
        // check if at edge and if blocked
        ChessPosition oneSpace = new ChessPosition( position.getRow() + isWhite, position.getColumn() );
        if ((!offBoard(oneSpace.getRow())) && (board.getPiece(oneSpace) != null)) {
            blockedAtOne = true;
        }
        ChessPosition twoSpaces = new ChessPosition( position.getRow() + 2 * isWhite, position.getColumn() );
        if ((!offBoard(twoSpaces.getRow())) && (board.getPiece(twoSpaces) != null)){
            blockedAtTwo = true;
        }
        if ((position.getRow() == pawnStartRow) && (!blockedAtOne) && !(blockedAtTwo)){
            // can optionally move two spaces forward
            possibleMoves.add( new ChessMove( position, new ChessPosition( position.getRow() + 2 * isWhite, position.getColumn()), null));
        }
        // can optionally move one space forward
        // if NOT blocked and NOT at edge
        if ((!blockedAtOne) && (!offBoard( position.getRow() + isWhite))){
            possibleMoves.add(new ChessMove(position, new ChessPosition(position.getRow() + isWhite, position.getColumn()), null));
        }
        // can capture diagonally
        if ((!offBoard(position.getRow() + isWhite)) && (!offBoard( position.getColumn() - 1 ))){
            ChessPosition diagonalLeft = new ChessPosition( position.getRow() + isWhite, position.getColumn() - 1);
            // IE if one space up and left exists on chess board
            if ((board.getPiece(diagonalLeft) != null) && (board.getPiece( diagonalLeft ).getTeamColor() == enemyColor )){
                // enemy piece, can capture
                possibleMoves.add( new ChessMove(position, diagonalLeft, null ));
            }
            // else if a friendly piece is at diagonalLeft or if empty, do nothing
        }
        if ((!offBoard( position.getRow() + isWhite))  && (!offBoard(position.getColumn() + 1))){
            ChessPosition diagonalRight = new ChessPosition( position.getRow() + isWhite, position.getColumn() + 1);
            // IE if one space up and left exists on the chess board
            if ((board.getPiece(diagonalRight) != null) && (board.getPiece( diagonalRight ).getTeamColor() == enemyColor )){
                // enemy piece, can capture
                possibleMoves.add( new ChessMove( position, diagonalRight, null) );
            }
            // else if a friendly piece is at diagonalRight or if empty, do nothing
        }
        return possibleMoves;
    }

    public ArrayList<ChessMove> calculatePawnMoves(){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        ChessPiece.PieceType[] promotionPieces = new ChessPiece.PieceType[4];

        // you can promote pawns to QUEEN, ROOK, BISHOP, or KNIGHT (who knew?)
        promotionPieces[0] = ChessPiece.PieceType.BISHOP;
        promotionPieces[1] = ChessPiece.PieceType.KNIGHT;
        promotionPieces[2] = ChessPiece.PieceType.QUEEN;
        promotionPieces[3] = ChessPiece.PieceType.ROOK;

        possibleMoves = findPawnMoves(board.getPiece( position ).getTeamColor() );

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
