package client;

import exception.ResponseException;
import model.GameData;
import repl.LoggedInRepl;
import repl.LoggedOutRepl;
import repl.Repl;
import requests.*;
import results.*;

import java.util.HashMap;
import java.util.Map;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
//    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;
    private Repl currentRepl;
    private String username = null;
    private String authToken = null;
    private HashMap<Integer, GameData> gameListDisplayed;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        currentRepl = new LoggedOutRepl( this );
        gameListDisplayed = new HashMap<Integer, GameData>();

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
                this.server.setAuthToken( this.authToken );
                this.getListOfGamesClient();
                this.start();
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
                    this.getListOfGamesClient();
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

    private void printGames(Map<Integer, GameData> games){
        System.out.println("Games:");
        for (Map.Entry<Integer, GameData> entry : games.entrySet()) {
            int i = entry.getKey();  // not gameID from db entry
            GameData game = entry.getValue();

            System.out.printf("%d. %s%n", i, game.gameName());
            System.out.printf("\twhite: %s%n", game.whiteUsername() != null ? game.whiteUsername() : "—");
            System.out.printf("\tblack: %s%n", game.blackUsername() != null ? game.whiteUsername() : "—");
            System.out.println();  // want to separate between game entries

        }
    }

    private void getListOfGamesClient(){
        try{
            this.gameListDisplayed.clear();
            ListGamesRequest request = new ListGamesRequest();
            ListGamesResult result = server.listGames( request );
            if ((result != null) && (result.games() != null)){
                for (int i = 0; i < result.games().size(); i++) {
                    this.gameListDisplayed.put(i + 1, result.games().get(i));
                }
            }
        } catch (ResponseException ex){
            // fixme: better message here
            System.out.println(ex.toString());
        }
    }


    public String listGamesClient(){
        getListOfGamesClient();
        printGames(this.gameListDisplayed);
        return "";
    }

    public String joinGameClient(String... params){
        if (this.gameListDisplayed.size() == 0){
            return "There are no games to join.  Try creating a game first.";
        }
        if (params.length < 2) {
            return "To join a game, use command 'join <game number> <white/black>'";
        }
        Integer i = 1;
        try {
            i = Integer.parseInt(params[0]);
        } catch (Exception ex) {
            return "game number must be an integer";
        }
        String color = params[1].toUpperCase();
        if ((!color.equals("WHITE")) && (!color.equals("BLACK"))){
            return "team color must be either 'white' or 'black' only.";
        }
        try{
            JoinGameRequest request = new JoinGameRequest(color, this.gameListDisplayed.get(i).gameID());
            JoinGameResult result = server.joinGame( request );
            if (result != null){
                return String.format("%s is now playing in game '%s' as %s",
                        this.username,
                        this.gameListDisplayed.get(i).gameName(),
                        color);
            }
        } catch (ResponseException ex){
            if (ex.code() == ResponseException.Code.AlreadyTaken){
                return String.format("Failed to join game %s because %s is already playing as %s.",
                        this.gameListDisplayed.get(i).gameName(),
                        color.equals("WHITE") ? this.gameListDisplayed.get(i).whiteUsername() : this.gameListDisplayed.get(i).blackUsername(),
                        color);
            }
            return "failed to join game";
        }
        return "failed to join game";
    }

//    private void assertSignedIn() throws ResponseException{
//        if (state == State.SIGNEDOUT){
//            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
//        }
//    }
}
