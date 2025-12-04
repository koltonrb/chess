package repl;

import client.ChessClient;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class PlayChessRepl implements Repl{
    private final ChessClient client;

    public PlayChessRepl(ChessClient client) { this.client = client; }

    @Override
    public String run() {
        System.out.print("-use \"help\" for command list");

        Scanner scanner = new Scanner(System.in);

        var result = "";
        while (!(result.equals("leave game") || result.equals("resign game")) ){
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = evalPlayChess(line);
                System.out.print(result);
            } catch (Throwable ex) {
                var msg = ex.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
        return result;
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> ");
    }

    public String evalPlayChess(String input) {
        String[] tokens = input.trim()
                .toLowerCase()
                .split("\\s+");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "draw" -> client.drawClient( params );
            case "highlight" -> client.highlightLegalMovesClient( params );
            case "leave" -> client.leaveClient( params );
            case "move" -> client.moveClient( params );
            case "resign" -> client.resignClient( params );
            default -> help();
        };
    }

    public String help() {
        return """
                \n
                SELECT GAME ACTION:
                -redraw the chess board "draw"
                -highlight legal moves for a given piece "highlight <col> <row>"
                -leave the game (you or another player may resume play later) "leave"
                -make a move in the game "move <current col> <current row> <destination col> <destination row>
                -forfeit the game "resign"
                -help with possible commands "help"
                """;
    }
}
