package client;

import exception.ResponseException;
import repl.LoggedInRepl;
import repl.LoggedOutRepl;
import repl.Repl;
import requests.CreateGameRequest;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
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
                if ((result != null) && (result.authToken() != null)){
                    state = State.SIGNEDIN;
                    this.username = result.username();
                    this.authToken = result.authToken();
                    this.currentRepl = new LoggedInRepl( this );
                    System.out.printf("You signed in as %s%n", result.username());
                    server.setAuthToken( this.authToken );
                    this.start();
                }
                return String.format("You signed in as %s", result.username());

            } catch (ResponseException ex) {
                if( ex.code() == ResponseException.Code.Unauthorized){
                    return "please provide a matching username/password combination.";
                }
                return "try logging in again with a valid username/password combination.";
            }
            catch (Throwable ex) {
                // TODO: get rid of the stack trace!
//            var msg = ex.toString();
//            System.out.print(msg);
                System.err.println("Logout failed at loginClient:");
                ex.printStackTrace();
            }
        }
        return "Expected valid username password combination";
    }

    public String logoutClient( String... params) throws ResponseException {
        LogoutRequest request = new LogoutRequest();
        // todo: should this be wrapped in a try/catch block?
        LogoutResult result = null;
        try {
            result = server.logoutUser(request);
        } catch (ResponseException ex) {
            return "failed to logout";
        } catch (Throwable ex) {
            // todo: get rid of the stack trace here
//            var msg = ex.toString();
//            System.out.print(msg);
            System.err.println("Logout failed at logoutClient:");
            ex.printStackTrace(); // This prints full stack trace to stderr
        }
        if ((result != null)) {
            state = State.SIGNEDOUT;
            this.username = null;
            this.authToken = null;
            this.server.setAuthToken( this.authToken );
            this.currentRepl = new LoggedOutRepl( this );
            System.out.println("You have logged out.");
            this.start();
        }
        return "You have logged out";
    }

    public String createGameClient( String ... params) {
        if (params.length < 1) {
            return "A name must be provided for the game to be created";
        }
        // FIXME: the authtoken is coming up as null...
        String gamename = params[0];
        try{
            CreateGameRequest request = new CreateGameRequest(gamename);
            CreateGameResult result = server.createGame( request );
            if ((result != null) && (result.gameID() != null)){
                return String.format("Game %s created successfully.", gamename);
            }
        } catch (ResponseException ex){
            return "Game creation failed.  You must provide a valid name for the game.";
        }
        return "Expected a valid name for the game";
    }

//    private void assertSignedIn() throws ResponseException{
//        if (state == State.SIGNEDOUT){
//            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
//        }
//    }
}
