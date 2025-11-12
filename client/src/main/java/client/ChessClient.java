package client;

import exception.ResponseException;
import repl.LoggedOutRepl;
import repl.Repl;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String visitorName = null;
    private final ServerFacade server;
//    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;
    private Repl currentRepl;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        currentRepl = new LoggedOutRepl( this );
//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void start(){
        currentRepl.run();
    }

//    public void loggedInRepl() {
//        Scanner scanner = new Scanner(System.in);
//        var result = "";
//        while (!result.equals("logout")){
//            printPrompt();
//            String line = scanner.nextLine();
//
//            try {
//                result = evalLoggedIn(line);
//                System.out.print(SET_TEXT_COLOR_GREEN + result);
//            } catch (Throwable ex) {
//                var msg = ex.toString();
//                System.out.print(msg);
//            }
//        }
//    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

//    public String evalLoggedOut(String input){
//        try{
//            String[] tokens = input.toLowerCase().split(" ");
//            String cmd = (tokens.length > 0) ? tokens[0] : "help";
//            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
//            return switch (cmd) {
//                case "register" -> registerClient( params );
//                case "login" -> loginClient( params );
//                case "quit" -> "quit";
//                default -> help();
//            };
//        } catch (ResponseException ex) {
//            return ex.getMessage();
//        }
//    }

    public String registerClient (String ... params) throws ResponseException {
        if (state != State.SIGNEDOUT){
            throw new ResponseException(ResponseException.Code.ClientError, "You are already signed in.  Sign out prior to registering another user.");
        }
        if (params.length >= 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];

            RegisterRequest request = new RegisterRequest(username, password, email);
            RegisterResult result = server.registerUser( request );
            if ((result != null) && (result.authToken() != null)){
                state = State.SIGNEDIN;
            }
            return String.format("new user %s registered successfully", result.username());
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected username, password, and email address");
    }

    public String loginClient( String... params) throws ResponseException {
        if (params.length >= 2) {
            String username = params[0];
            String password = params[1];

            LoginRequest request = new LoginRequest(username, password);
            // todo: should this be wrapped in a try/catch block?
            LoginResult result = server.loginUser( request );
            if ((result != null) && (result.authToken() != null)){
                state = State.SIGNEDIN;
            }
            return String.format("You signed in as %s.", result.username());
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected valid username password combination");
    }

//    private void assertSignedIn() throws ResponseException{
//        if (state == State.SIGNEDOUT){
//            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
//        }
//    }
}
