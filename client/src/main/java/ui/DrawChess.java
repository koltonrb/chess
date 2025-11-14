package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

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
    private final ChessGame.TeamColor perspective;
    private final List<String> COLUMNS;

    public static final Map<ChessPiece.PieceType, String> PIECE_TO_STRING = Map.of(
            ChessPiece.PieceType.PAWN,   "P",
            ChessPiece.PieceType.ROOK,   "R",
            ChessPiece.PieceType.KNIGHT, "N",
            ChessPiece.PieceType.BISHOP, "B",
            ChessPiece.PieceType.QUEEN,  "Q",
            ChessPiece.PieceType.KING,   "K"
    );

    private ChessGame.TeamColor current_square_color;

    public DrawChess(ChessBoard board, ChessGame.TeamColor perspective) {
        this.board = board;
        this.perspective = perspective;

        if (this.perspective == ChessGame.TeamColor.WHITE) {
            this.COLUMNS = WHITE_COLUMNS;
        } else {
            this.COLUMNS = BLACK_COLUMNS;
        }
    }


    public String main(){
        StringBuilder sb = new StringBuilder();

        sb.append(ERASE_SCREEN);
        drawHeaders(sb);
        drawBoard(sb);
        drawHeaders(sb);

        return sb.toString();
    }

    private void drawHeaders(StringBuilder sb){
        setBoarder(sb);
        sb.append(EMPTY); // for left hand row labels
        sb.append(EMPTY);
        for (String col : this.COLUMNS){
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

        for (int squareRow = startRow;
             (rowIncrement > 0) ? (squareRow < endRow) : (squareRow > endRow);
             squareRow += rowIncrement){

            setBoarder(sb);
            sb.append(String.format("%s ", squareRow + 1));
            if (squareRow % 2 == 0){
                // then white square first
                current_square_color = ChessGame.TeamColor.WHITE;
                setWhiteSpace(sb);
            } else {
                current_square_color = ChessGame.TeamColor.BLACK;
                setBlackSpace(sb);
            }
            sb.append(SET_TEXT_BOLD);
            for (int boardCol = 7; boardCol >= 0; --boardCol){

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
                current_square_color = current_square_color == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                // now I have next square color stored in current_square_color
                if (current_square_color == ChessGame.TeamColor.WHITE){
                    setWhiteSpace(sb);
                } else {
                    setBlackSpace(sb);
                }
            }
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

}
