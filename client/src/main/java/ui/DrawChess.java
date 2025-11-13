package ui;

import chess.ChessBoard;
import chess.ChessGame;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ui.EscapeSequences.*;

public class DrawChess {
    // board dimensions
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 2;
    private static final int LINE_WIDTH_IN_PADDED_CHARS = 1;

    // padded characters
    private static final String EMPTY = " ";  // should be em space?
    // todo more here?  define the characters for each piece type and team?

    private static final List<String> WHITE_COLUMNS = List.of("A", "B", "C", "D", "E", "F", "G", "H");
    private static final List<String> BLACK_COLUMNS = List.of("H", "G", "F", "E", "D", "C", "B", "A");

    private final ChessBoard board;
    private final ChessGame.TeamColor perspective;
    private final List<String> COLUMNS;

    public DrawChess(ChessBoard board, ChessGame.TeamColor perspective) {
        this.board = board;
        this.perspective = perspective;

        if (this.perspective == ChessGame.TeamColor.WHITE) {
            this.COLUMNS = WHITE_COLUMNS;
        } else {
            this.COLUMNS = BLACK_COLUMNS;
        }
    }


    public void main(){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);
        drawHeaders(out);
    }

    private void drawHeaders(PrintStream out){
        setBoarder(out);
        out.print(EMPTY); // for left hand row labels
        for (String col : this.COLUMNS){
            out.printf("%s ", col);
        }
        out.print(EMPTY);  // for right hand row labels
    }

    private void drawRowOfSqaures(PrintStream out){
        for (int squareRow = 0; squareRow < SQUARE_SIZE_IN_PADDED_CHARS; ++squareRow){
            setBoarder(out);
            out.printf("%d ", squareRow);

            for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol){
                setWhite(out);

            }
        }
    }

    private void setBoarder(PrintStream out){
        out.print(SET_BG_COLOR_DARK_GREY);
        out.print(SET_TEXT_COLOR_LIGHT_GREY);
    }
}
