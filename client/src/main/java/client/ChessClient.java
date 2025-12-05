package client;

import chess.ChessGame;
import model.GameData;
import repl.LoggedInRepl;
import repl.LoggedOutRepl;
import repl.PlayChessRepl;
import repl.Repl;
import requests.*;
import results.*;
import ui.DrawChess;
import websocket.messages.ServerMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;
    private Repl currentRepl;
    private String username = null;
    private String authToken = null;
    private HashMap<Integer, GameData> gameListDisplayed;

    public ChessClient(int port) throws ResponseException {
        server = new ServerFacade(port);
        ws = new WebSocketFacade(server.getServerUrl(), this);
        currentRepl = new LoggedOutRepl( this );
        gameListDisplayed = new HashMap<Integer, GameData>();

//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void start(){
        currentRepl = new LoggedOutRepl( this );
        Scanner scanner = new Scanner(System.in);

        while(true){
            String result = currentRepl.run();
            if (result.equals("quit")){
                System.out.println("Goodbye");
                break;
            }
            if (result.contains("You signed in as")  || result.contains("registered successfully") || result.equals("leave game") || result.equals("resign game")){
                currentRepl = new LoggedInRepl( this);
            } else if (result.contains("logged out") || result.equals("logout")){
                currentRepl = new LoggedOutRepl( this );
            } else if (result.contains("now playing in game")){
                currentRepl = new PlayChessRepl( this );
            }
        }
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String registerClient (String ... params) {
        if (state != State.SIGNEDOUT){
            return "You are already signed in.  Sign out prior to registering another user.";
        }
        String username;
        String password;
        String email;
        if (params.length >= 3) {
            username = params[0];
            password = params[1];
            email = params[2];
        } else {
            return "Failed to register new user.  Expected format \"register <username> <password> <email>\". All three fields must be provided.";
        }
        RegisterRequest request = new RegisterRequest(username, password, email);
        try {
            RegisterResult result = server.registerUser(request);
            if ((result != null) && (result.authToken() != null)) {
                state = State.SIGNEDIN;
                this.username = result.username();
                this.authToken = result.authToken();
//                this.currentRepl = new LoggedInRepl(this);
                this.server.setAuthToken(this.authToken);
                this.getListOfGamesClient();
//                this.start();
                return String.format("new user %s registered successfully", result.username());
            }
        } catch (ResponseException ex){
            if (ex.code() == ResponseException.Code.AlreadyTaken){
                return String.format("Failed to register new user because username \"%s\" is already taken.", username);
            } else if (ex.code() == ResponseException.Code.BadRequest) {
                return "Failed to register new user.  Expected format \"register <username> <password> <email>\". All three fields must be provided.";
            } else {
                return "Failed to register new user.  Expected format \"register <username> <password> <email>\".";
            }
        } catch (Throwable ex){
            return ex.getMessage();
        }

        return "Expected command \"register\" <username> <password> <email>";
    }

    public String loginClient( String... params) {
        if (params.length >= 2) {
            String username = params[0];
            String password = params[1];

            LoginRequest request = new LoginRequest(username, password);
            LoginResult result = null;
            try {
                result = server.loginUser(request);
                if ((result != null) && (result.authToken() != null)){
                    state = State.SIGNEDIN;
                    this.username = result.username();
                    this.authToken = result.authToken();
//                    this.currentRepl = new LoggedInRepl( this );
//                    System.out.printf("You signed in as %s%n", result.username());
                    server.setAuthToken( this.authToken );
                    this.getListOfGamesClient();
//                    this.start();
                }
                return String.format("You signed in as %s", result.username());

            } catch (ResponseException ex) {
                if( ex.code() == ResponseException.Code.Unauthorized){
                    return "please provide a matching username/password combination.";
                }
                return "try logging in again with a valid username/password combination.";
            }
        }
        return "Expected valid username password combination";
    }

    public String logoutClient( String... params) {
        LogoutRequest request = new LogoutRequest();
        LogoutResult result = null;
        try {
            result = server.logoutUser(request);
        } catch (ResponseException ex) {
            return "failed to logout";
        }
        if ((result != null)) {
            state = State.SIGNEDOUT;
            this.username = null;
            this.authToken = null;
            this.server.setAuthToken( this.authToken );
            return "You have logged out.";
        }
        return "";
    }

    public String createGameClient( String ... params) {
        if (params.length < 1) {
            return "A game name must be provided for the game to be created";
        }
        String gamename = params[0];
        try{
            CreateGameRequest request = new CreateGameRequest(gamename);
            CreateGameResult result = server.createGame( request );
            if ((result != null) && (result.gameID() != null)){
                this.getListOfGamesClient();  // this will allow user to join game with the next game number without calling list
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
            System.out.printf("\tblack: %s%n", game.blackUsername() != null ? game.blackUsername() : "—");
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
            System.out.println("Error listing games.  Use command \"list\"");
        }
    }


    public String listGamesClient(){
        getListOfGamesClient();
        printGames(this.gameListDisplayed);
        return "";
    }

    public String joinGameClient(String... params){
        getListOfGamesClient();  // will reflect up to date changes in game list
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
        if (!gameListDisplayed.containsKey(i)){
            return String.format("game %d does not exist. \nPlease enter a game number between %d and %d",
                    i,
                    Collections.min(gameListDisplayed.keySet() ),
                    Collections.max(gameListDisplayed.keySet() ));
        }
        String color = params[1].toUpperCase();
        if ((!color.equals("WHITE")) && (!color.equals("BLACK"))){
            return "team color must be either 'white' or 'black' only.";
        }
        try{
            JoinGameRequest request = new JoinGameRequest(color, this.gameListDisplayed.get(i).gameID());
            JoinGameResult result = server.joinGame( request );
            if (result != null){
                String boardToPrint = "";
                boardToPrint = new DrawChess(this.gameListDisplayed.get(i).game().getBoard(),
                        color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK).main();
                getListOfGamesClient();  // will reflect that player is now in a game on the list
                ws.connectToGame(this.authToken, this.gameListDisplayed.get(i).gameID(), color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);
                return String.format("%s is now playing in game '%s' as %s\n\n%s",
                        this.username,
                        this.gameListDisplayed.get(i).gameName(),
                        color,
                        boardToPrint);
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

    public String observeGameClient(String... params){
        if (this.gameListDisplayed.size() == 0){
            return "There are no games to observe.";
        }
        if (params.length < 1) {
            return "To observe a game, use command 'observe <game number>'";
        }
        Integer i = 1;
        try {
            i = Integer.parseInt(params[0]);
        } catch (Exception ex) {
            return "game number must be an integer";
        }
        if (!gameListDisplayed.containsKey(i)){
            return String.format("game %d does not exist. \nPlease enter a game number between %d and %d",
                    i,
                    Collections.min(gameListDisplayed.keySet() ),
                    Collections.max(gameListDisplayed.keySet() ));
        }

        String boardToPrint = new DrawChess(this.gameListDisplayed.get(i).game().getBoard(),
                                                ChessGame.TeamColor.WHITE).main();

        return boardToPrint;
    }

    public String drawClient(String... params){
        return "YOU NEED TO IMPLEMENT drawClient STILL";
    }

    public String highlightLegalMovesClient(String... params){
        return "YOU NEED TO IMPLEMENT highlightLegalMovesClient STILL";
    }

    public String leaveClient(String... params){
        // TODO: this needs to set the chess color player name to null in the db
        return "leave game";
    }

    public String moveClient(String... params){
        return "YOU NEED TO IMPLEMENT moveClient STILL";
    }

    public String resignClient(String... params){
        // TODO: this needs to to other chess things to resign the game
        return "resign game";
    }

    public void notify(ServerMessage message){
        System.out.println(message.toString());
    }

}
