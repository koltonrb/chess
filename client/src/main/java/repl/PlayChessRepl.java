package repl;

import client.ChessClient;

import java.util.Scanner;

import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class PlayChessRepl implements Repl{
    private final ChessClient client;

    public PlayChessRepl(ChessClient client) { this.client = client; }

    @Override
    public String run() {
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);

        var result = "";
        while (!(result.contains("Leave") || result.contains("Resign")) ){
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

    public String eval
}
