package repl;

import client.ChessClient;
import client.State;
import exception.ResponseException;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class LoggedOutRepl implements Repl{
    private final ChessClient client;

    public LoggedOutRepl(ChessClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println("Welcome to Chess! Sign in or register to begin.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")){
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = evalLoggedOut(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable ex) {
                var msg = ex.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_BLUE);
    }

    public String evalLoggedOut(String input){
        try{
            String[] tokens = input.trim()
                    .toLowerCase()
                    .split("\\s+");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> client.registerClient( params );
                case "login" -> client.loginClient( params );
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        return """
                   -register as new user "register <username> <password> <email>"
                   -login to view and play chess "login <username> <password>"
                   -exit the program "quit"
                   -help with possible commands "help"
                   """;
        }
}

