package ui;

import chess.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ui.EscapeSequences.*;

public class DrawChess {
    // board dimensions
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 3;
    private static final int LINE_WIDTH_IN_PADDED_CHARS = 1;

    // padded characters
    private static final String EMPTY = " ";  // should be em space?
    private static final List<String> WHITE_COLUMNS = List.of("A", "B", "C", "D", "E", "F", "G", "H");
    private static final List<String> BLACK_COLUMNS = List.of("H", "G", "F", "E", "D", "C", "B", "A");

    private final ChessBoard board;
    private ChessGame.TeamColor perspective;
    private final List<String> columnsForPerspective;

    public static final Map<ChessPiece.PieceType, String> PIECE_TO_STRING = Map.of(
            ChessPiece.PieceType.PAWN,   "P",
            ChessPiece.PieceType.ROOK,   "R",
            ChessPiece.PieceType.KNIGHT, "N",
            ChessPiece.PieceType.BISHOP, "B",
            ChessPiece.PieceType.QUEEN,  "Q",
            ChessPiece.PieceType.KING,   "K"
    );

    public DrawChess(ChessBoard board, ChessGame.TeamColor perspective) {
        this.board = board;
//        this.perspective = perspective;
        this.perspective = (perspective != null) ? perspective : ChessGame.TeamColor.WHITE;

        if ((this.perspective == ChessGame.TeamColor.WHITE)) {
            // null for an observer?
            this.columnsForPerspective = WHITE_COLUMNS;
        } else {
            this.columnsForPerspective = BLACK_COLUMNS;
        }
    }


    public String main(){
        StringBuilder sb = new StringBuilder();

        sb.append(ERASE_SCREEN);
        drawHeaders(sb);
        drawBoard(sb);
        drawHeaders(sb);

        // THIS IS THE KEY LINE
        sb.append(RESET_BG_COLOR)
                .append(RESET_TEXT_COLOR)
                .append(RESET_TEXT_BOLD_FAINT);

        return sb.toString();
    }

    private boolean validEndPosition(ArrayList<ChessMove> moves, ChessPosition position){
        for (ChessMove move: moves){
            if (move.getEndPosition().equals( position )){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public String highlightMoves(ChessPosition squareOfInterest, ChessGame game){
        // get a list of valid moves for the given squareOfInterest
        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();
        if (board.getPiece( squareOfInterest )!= null){
            validMoves = (ArrayList<ChessMove>) game.validMoves( squareOfInterest );
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ERASE_SCREEN);
        drawHeaders(sb);

        int startRow = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 7;
            case ChessGame.TeamColor.BLACK -> 0;
        };
        int rowIncrement = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> -1;
            case ChessGame.TeamColor.BLACK -> 1;
        };
        int endRow = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> -1;
            case ChessGame.TeamColor.BLACK -> 8;
        };

        int startCol = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 0;
            case ChessGame.TeamColor.BLACK -> 7;
        };
        int colIncrement = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 1;
            case ChessGame.TeamColor.BLACK -> -1;
        };
        int endCol = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 8;
            case ChessGame.TeamColor.BLACK -> -1;
        };

        ChessGame.TeamColor nextSquareColor = ChessGame.TeamColor.WHITE;

        for (int squareRow = startRow;
             (rowIncrement > 0) ? (squareRow < endRow) : (squareRow > endRow);
             squareRow += rowIncrement){

            setBoarder(sb);
            sb.append(String.format("%s ", squareRow + 1));
            if (nextSquareColor == ChessGame.TeamColor.WHITE){
                setWhiteSpace(sb);
            } else {
                setBlackSpace(sb);
            }
            sb.append(SET_TEXT_BOLD);
            for (int boardCol = startCol;
                 (colIncrement < 0) ? (boardCol > endCol) : (boardCol < endCol);
                 boardCol += colIncrement){
                // check to see if your current (row, col) is your squareOfInterest of interest:
                if (squareOfInterest.equals( new ChessPosition(squareRow + 1, boardCol+1))){
                    if (nextSquareColor == ChessGame.TeamColor.WHITE) {
                        setYellowSpace(sb);
                    } else {
                        setDarkYellowSpace(sb);
                    }
                } else if ( validEndPosition(validMoves, new ChessPosition( squareRow + 1, boardCol + 1) )) {
                    // IE then this square is somewhere the highlighted piece can move to
                    if (nextSquareColor == ChessGame.TeamColor.WHITE){
                        //IE you are printing a light square
                        setLightHighlightSpace(sb);
                    } else{
                        setDarkHightSpace(sb);
                    }
                }

                sb.append(EMPTY);
                ChessPiece piece = this.board.getPiece(new ChessPosition(squareRow + 1, boardCol +1));
                if (piece != null){
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        sb.append(SET_TEXT_COLOR_BLUE);
                    } else {
                        sb.append(SET_TEXT_COLOR_RED);
                    }
                    sb.append(PIECE_TO_STRING.get( piece.getPieceType() ));
                } else {
                    sb.append(EMPTY);
                }
                sb.append(EMPTY);
                nextSquareColor = nextSquareColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                if (nextSquareColor == ChessGame.TeamColor.WHITE) {
                    setWhiteSpace(sb);
                } else {
                    setBlackSpace(sb);
                }
            }
            // when starting a new row, you repeat the color of squareOfInterest that ended the previous row
            nextSquareColor = nextSquareColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            sb.append(RESET_TEXT_BOLD_FAINT);
            setBoarder(sb);
            sb.append(String.format(" %s", squareRow + 1));
            sb.append("\n");
        }
        drawHeaders(sb);
        sb.append(RESET_BG_COLOR)
                .append(RESET_TEXT_COLOR)
                .append(RESET_TEXT_BOLD_FAINT);
        return sb.toString();
    }

    private void drawHeaders(StringBuilder sb){
        setBoarder(sb);
        sb.append(EMPTY); // for left hand row labels
        sb.append(EMPTY);
        for (String col : this.columnsForPerspective){
            sb.append(String.format(EMPTY + "%s" + EMPTY, col));
        }
        sb.append(EMPTY);  // for right hand row labels
        sb.append(EMPTY);
        sb.append("\n");
    }

    private void drawBoard(StringBuilder sb){

        int startRow = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 7;
            case ChessGame.TeamColor.BLACK -> 0;
        };
        int rowIncrement = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> -1;
            case ChessGame.TeamColor.BLACK -> 1;
        };
        int endRow = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> -1;
            case ChessGame.TeamColor.BLACK -> 8;
        };

        int startCol = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 0;
            case ChessGame.TeamColor.BLACK -> 7;
        };
        int colIncrement = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 1;
            case ChessGame.TeamColor.BLACK -> -1;
        };
        int endCol = switch (this.perspective) {
            case ChessGame.TeamColor.WHITE -> 8;
            case ChessGame.TeamColor.BLACK -> -1;
        };

        ChessGame.TeamColor nextSquareColor = ChessGame.TeamColor.WHITE;

        for (int squareRow = startRow;
             (rowIncrement > 0) ? (squareRow < endRow) : (squareRow > endRow);
             squareRow += rowIncrement){

            setBoarder(sb);
            sb.append(String.format("%s ", squareRow + 1));
            if (nextSquareColor == ChessGame.TeamColor.WHITE){
                setWhiteSpace(sb);
            } else {
                setBlackSpace(sb);
            }
            sb.append(SET_TEXT_BOLD);
            for (int boardCol = startCol;
                 (colIncrement < 0) ? (boardCol > endCol) : (boardCol < endCol);
                 boardCol += colIncrement){
                sb.append(EMPTY);
                ChessPiece piece = this.board.getPiece(new ChessPosition(squareRow + 1, boardCol +1));
                if (piece != null){
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        sb.append(SET_TEXT_COLOR_BLUE);
                    } else {
                        sb.append(SET_TEXT_COLOR_RED);
                    }
                    sb.append(PIECE_TO_STRING.get( piece.getPieceType() ));
                } else {
                    sb.append(EMPTY);
                }
                sb.append(EMPTY);


                nextSquareColor = nextSquareColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                if (nextSquareColor == ChessGame.TeamColor.WHITE) {
                    setWhiteSpace(sb);
                } else {
                    setBlackSpace(sb);
                }

            }
            // when starting a new row, you repeat the color of square that ended the previous row
            nextSquareColor = nextSquareColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            sb.append(RESET_TEXT_BOLD_FAINT);
            setBoarder(sb);
            sb.append(String.format(" %s", squareRow + 1));
            sb.append("\n");
        }
    }

    private void setBoarder(StringBuilder sb){

        sb.append(SET_BG_COLOR_DARK_GREY);
        sb.append(SET_TEXT_COLOR_LIGHT_GREY);
    }

    private void setWhiteSpace(StringBuilder sb){
        sb.append(SET_BG_COLOR_WHITE);
    }

    private void setBlackSpace(StringBuilder sb){
        sb.append(SET_BG_COLOR_BLACK);
    }

    private void setYellowSpace(StringBuilder sb){
        sb.append(SET_BG_COLOR_YELLOW);
    }

    private void setDarkYellowSpace(StringBuilder sb){
        sb.append(SET_BG_COLOR_DARK_YELLOW);
    }

    private void setLightHighlightSpace(StringBuilder sb){
        sb.append(SET_BG_COLOR_GREEN);
    }

    private void setDarkHightSpace(StringBuilder sb){
        sb.append(SET_BG_COLOR_DARK_GREEN);
    }


}
