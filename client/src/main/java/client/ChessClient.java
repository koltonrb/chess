package client;

import exception.ResponseException;
import repl.LoggedInRepl;
import repl.LoggedOutRepl;
import repl.Repl;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.LogoutResult;
import results.RegisterResult;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
//    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;
    private Repl currentRepl;
    private String username = null;
    private String authToken = null;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        currentRepl = new LoggedOutRepl( this );
//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void start(){
        currentRepl.run();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

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
                this.username = result.username();
                this.authToken = result.authToken();
                this.currentRepl = new LoggedInRepl( this );
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
            LoginResult result = null;
            try {
                result = server.loginUser(request);
            } catch (Throwable ex) {
//            var msg = ex.toString();
//            System.out.print(msg);
                System.err.println("Logout failed at logoutClient:");
                ex.printStackTrace();
            }
            if ((result != null) && (result.authToken() != null)){
                state = State.SIGNEDIN;
                this.username = result.username();
                this.authToken = result.authToken();
                this.currentRepl = new LoggedInRepl( this );
                this.start();
            }
            return String.format("You signed in as %s.", result.username());
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected valid username password combination");
    }

    public String logoutClient( String... params) throws ResponseException {
        LogoutRequest request = new LogoutRequest();
        // todo: should this be wrapped in a try/catch block?
        LogoutResult result = null;
        try {
            server.setAuthToken( this.authToken );
            result = server.logoutUser(request);
        } catch (Throwable ex) {
//            var msg = ex.toString();
//            System.out.print(msg);
            System.err.println("Logout failed at logoutClient:");
            ex.printStackTrace(); // This prints full stack trace to stderr
        }
        if ((result != null)) {
            state = State.SIGNEDOUT;
            this.username = null;
            this.authToken = null;
            this.currentRepl = new LoggedOutRepl( this );
            this.start();
        }
        return "You have logged out.";
    }

//    private void assertSignedIn() throws ResponseException{
//        if (state == State.SIGNEDOUT){
//            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
//        }
//    }
}
