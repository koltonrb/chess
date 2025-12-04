package repl;

import client.ChessClient;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class LoggedInRepl implements Repl {
    private final ChessClient client;

    public LoggedInRepl(ChessClient client){
        this.client = client;
    }

    @Override
    public String run() {
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        // todo: update this while condition to swap repls when observing a game
        while (!(result.contains("You have logged out") || result.contains("now playing in game")) ){
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = evalLoggedIn(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
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

    public String evalLoggedIn(String input){
        String[] tokens = input.trim()
                .toLowerCase()
                .split("\\s+");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "logout" -> client.logoutClient( params );
            case "create" -> client.createGameClient( params );
            case "list" -> client.listGamesClient();
            case "play" -> client.joinGameClient( params );
            case "observe" -> client.observeGameClient( params );
            default -> help();
        };
}

    public String help() {
        return """
                \n
                SELECT GAME ACTION:
                -end session "logout"
                -create a new chess game "create <game name>"
                -list current chess games and players "list"
                -join an existing chess game "play <game number> <white/black>"
                -watch a game "observe"
                -help with possible commands "help"
                """;
    }
}
