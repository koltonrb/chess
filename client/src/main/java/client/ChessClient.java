package client;

import exception.ResponseException;
import ui.EscapeSequences;

import java.net.http.WebSocket;
import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String visitorName = null;
    private final ServerFacade server;
//    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to Chess! Sign in or register to begin.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")){
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
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

    public String eval(String input){
        try{
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> registerClient( params );
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String registerClient (String ... params){

        if (params.length >= 3) {
            String username = params[0];
        }
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            //todo: fix me: add better pre-login help message
            return """
                   -Help (h)
                   -Quit (q)
                   -Login (login)
                   -Register (register)
                   """;
        }
        // todo: add better post-login help
        // maybe add a third state for in a game, too?
        return """
                -display logged in help here!
                """;
    }

    private void assertSignedIn() throws ResponseException{
        if (state == State.SIGNEDOUT){
            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
        }
    }
}
