package repl;

import client.ChessClient;
import exception.ResponseException;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class LoggedInRepl implements Repl {
    private final ChessClient client;

    public LoggedInRepl(ChessClient client){
        this.client = client;
    }

    @Override
    public void run() {
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("logout")){
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
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String evalLoggedIn(String input){
        try{
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "logout" -> client.logoutClient( params );
                case "create" -> client.createGameClient( params );
                case "list" -> client.listGamesClient();
                case "play" -> client.joinGameClient( params );
//                case "observe" -> client.observeGameClient( params );
                default -> help();
            };
            } catch (ResponseException ex) {
                return ex.getMessage();
        }
    }

    public String help() {
        //TODO add better logged in help cues.
        return """
                -logout
                -create game "create <game name>"
                -list games "list" 
                -play game 
                -observe game 
                """;
    }
}
