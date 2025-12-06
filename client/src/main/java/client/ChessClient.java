package client;

import chess.*;
import model.GameData;
import repl.LoggedInRepl;
import repl.LoggedOutRepl;
import repl.PlayChessRepl;
import repl.Repl;
import requests.*;
import results.*;
import ui.DrawChess;
import websocket.messages.LoadGameMessage;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;
    private Repl currentRepl;
    private String username = null;
    private String authToken = null;
    private HashMap<Integer, GameData> gameListDisplayed;
    private ChessGame.TeamColor perspective = ChessGame.TeamColor.WHITE;
    private Integer gameNumber;
    private Boolean isPlaying = Boolean.FALSE;
    private Boolean hasResigned = Boolean.FALSE;
    private GameData currentGame;

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
            } else if (result.contains("now playing in game") || result.contains("now observing game")){
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
                int gameNumber = 1; //to display NOT gameID numbers
                for (GameData game: result.games()){
                    if (game.canUpdate()){
                        this.gameListDisplayed.put(gameNumber, game);
                        gameNumber++;
                    }
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
        this.gameNumber = i;
        String color = params[1].toUpperCase();
        if ((!color.equals("WHITE")) && (!color.equals("BLACK"))){
            return "team color must be either 'white' or 'black' only.";
        }
        this.perspective = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        try{
            JoinGameRequest request = new JoinGameRequest(color, this.gameListDisplayed.get(i).gameID());
            JoinGameResult result = server.joinGame( request );
            if (result != null){
//                String boardToPrint = "";
//                boardToPrint = new DrawChess(this.gameListDisplayed.get(i).game().getBoard(),
//                        color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK).main();
                getListOfGamesClient();  // will reflect that player is now in a game on the list
                ws.connectToGame(this.authToken, this.gameListDisplayed.get(i).gameID(), color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);
                this.isPlaying = Boolean.TRUE;
                this.currentGame = this.gameListDisplayed.get(i);
                this.hasResigned = Boolean.FALSE;
                return String.format("%s is now playing in game '%s' as %s",
                        this.username,
                        this.gameListDisplayed.get(i).gameName(),
                        color
                        );
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
        this.perspective = null;
        this.gameNumber = i;
        String boardToPrint = new DrawChess(this.gameListDisplayed.get(i).game().getBoard(),
                                                ChessGame.TeamColor.WHITE).main();

        try{
            ws.connectToGame(this.authToken, this.gameListDisplayed.get(i).gameID(), null);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
        this.isPlaying = Boolean.FALSE;
        this.currentGame = this.gameListDisplayed.get(i);
        System.out.println( boardToPrint );
        return String.format("now observing game %s", this.gameListDisplayed.get(i).gameName());
    }

    public String drawClient(String... params){
        getListOfGamesClient();  // will reflect up to date changes in game list
        String boardToPrint = "";
        if ((this.gameListDisplayed.get(this.gameNumber) != null)
            && (Objects.equals(this.gameListDisplayed.get(this.gameNumber).gameID(), this.currentGame.gameID()))) {
            // then fetch and draw the most up to date version
            boardToPrint = new DrawChess(this.gameListDisplayed.get(this.gameNumber).game().getBoard(),
                            this.perspective != null ? this.perspective : ChessGame.TeamColor.WHITE).main();
        } else {
            // draw what the client last received from the server
            boardToPrint = new DrawChess(this.currentGame.game().getBoard(),
                    this.perspective != null ? this.perspective : ChessGame.TeamColor.WHITE).main();
        }
        return boardToPrint;
    }

    public String highlightLegalMovesClient(String... params){
        // todo
        return "YOU NEED TO IMPLEMENT highlightLegalMovesClient STILL";
    }

    public String leaveClient(String... params){
        getListOfGamesClient();  // will reflect up to date changes in game list
//        GameData currentGame = gameListDisplayed.get(this.gameNumber);
        GameData updatedGame;
        if (this.perspective != null) {
            // IE if you were playing the game as BLACK or WHITE
            if (this.perspective == ChessGame.TeamColor.WHITE){
                updatedGame = new GameData(currentGame.gameID(),
                        null,
                        currentGame.blackUsername(),
                        currentGame.gameName(),
                        currentGame.game(),
                        currentGame.canUpdate());
            } else {
                updatedGame = new GameData(currentGame.gameID(),
                        currentGame.whiteUsername(),
                        null,
                        currentGame.gameName(),
                        currentGame.game(),
                        currentGame.canUpdate());
            }
            UpdateGameRequest request = new UpdateGameRequest( updatedGame );
            try {
                UpdateGameResult result = server.updateGame(request);
            } catch (ResponseException ex) {
                return "Failed to exit the game";
            }
        } else {
            // no updates necessary to the game data if observer leaves
            updatedGame = currentGame;
        }
        try {
            ws.leaveGame(this.authToken, updatedGame.gameID(), this.perspective);
        } catch (ResponseException e) {
            return "Failed to broadcast game exit";
        }
        this.isPlaying = Boolean.FALSE;
        this.currentGame = null;
        return "leave game";
    }

    public String moveClient(String... params){
        // fixme update gamelist here?
        if (this.currentGame.game().getTeamTurn() != this.perspective){
            return "it is not your turn to play!";
        }
        if (params.length < 2){
            return "you must provide directions for your army! <start file><start rank> <end file><end rank> <promotion?>";
        }
        ChessPiece.PieceType promoPiece;
        if (params.length > 3){
            return "you must provide both a start space and an end space like A1 B1 <start file><start rank> <end file><end rank> <promotion?>";
        }
        String start = params[0].trim().toUpperCase();
        String end = params[1].trim().toUpperCase();
        if (params.length > 2) {
            String promoString = params[2].trim().toUpperCase(); // remember zero indexed
            // check if promotion piece is valid
            if (!(promoString.equals("B") || promoString.equals("N") || promoString.equals("R") || promoString.equals("Q"))){
                return "valid promotion pieces are B, N, R, Q";
            }
            HashMap<String, ChessPiece.PieceType> promotionPieces = new HashMap<String, ChessPiece.PieceType>();
            promotionPieces.put("B", ChessPiece.PieceType.BISHOP);
            promotionPieces.put("N", ChessPiece.PieceType.KNIGHT);
            promotionPieces.put("R", ChessPiece.PieceType.ROOK);
            promotionPieces.put("Q", ChessPiece.PieceType.QUEEN);

            promoPiece = promotionPieces.get(promoString);

        } else {
            String promoString = null;
            promoPiece = null;
        }
        // check if input is letter+number
        if ((start.length() != 2 ) || (end.length() != 2)){
            return "acceptable start and end spaces are A1 through H8.  Provide <start file><start rank> <end file><end rank> <promotion?>";
        }
        char startColTemp = Character.toUpperCase(start.charAt(0));
        char endColTemp = Character.toUpperCase(end.charAt(0));
        if (startColTemp < 'A' || endColTemp > 'H'){
            return "valid files (columns) are A through H only. Provide <start file><start rank> <end file><end rank> <promotion?>";
        }
        Integer startRowTemp;
        Integer endRowTemp;
        try {
            startRowTemp = Integer.parseInt(String.valueOf(start.charAt(1)));
            endRowTemp = Integer.parseInt(String.valueOf(end.charAt(1)));
        } catch (Exception ex){
            return "valid ranks (rows) are 1 through 8. Provide <start file><start rank> <end file><end rank> <promotion?>";
        }
        if (startRowTemp < 1 || startRowTemp > 8 || endRowTemp < 1 || endRowTemp > 8){
            return "valid ranks (rows) are 1 through 8. Provide <start file><start rank> <end file><end rank> <promotion?>";
        }
        ChessPosition startSquare = new ChessPosition(start);
        ChessPosition endSquare = new ChessPosition(end);
        ChessMove desiredMove = new ChessMove(startSquare, endSquare, promoPiece);

        // else make the move!
        // this valid move check should be done ON THE SERVER TODO
//        try {
//            this.currentGame.game().makeMove(desiredMove);
//        } catch (InvalidMoveException e) {
//            return "that is not a valid move. Try again";
//        }
//        UpdateGameRequest request = new UpdateGameRequest( this.currentGame );
//        try {
//            // fixme: may need a separate make move update method?
//            UpdateGameResult result = server.updateGame( request );
//        } catch (ResponseException e) {
//            return "failed to make the move";
//        }
//
//        // now share the move!
//        try {
//            ws.makeMove(this.authToken, this.currentGame.gameID(), start, end, desiredMove);
//        } catch (ResponseException e) {
//            return "failed to broadcast move";
//        }
        try {
            ws.makeMove(this.authToken, this.currentGame.gameID(), start, end, desiredMove, this.perspective);
        } catch (ResponseException e) {
            return "failed to make or report the move";
        }
        // check if the move is valid
        if (this.currentGame.game().getBoard().getPiece(startSquare) == null){
            return String.format("there is no piece at %s.", start);
        }
        if ((this.currentGame.game().getBoard().getPiece(startSquare) != null)
                &&( this.currentGame.game().getBoard().getPiece(startSquare).getTeamColor() != this.perspective)){
            return String.format("you can only move your team's pieces");
        }
        ArrayList<ChessMove> validMoves = (ArrayList<ChessMove>) this.currentGame.game().validMoves(startSquare);
//        ChessMove desiredMove = new ChessMove(startSquare, endSquare, promoPiece);
        Boolean moveIsValid = Boolean.FALSE;
        for (ChessMove move : validMoves){
            if (move.equals(desiredMove)){
                moveIsValid = Boolean.TRUE;
                break;
            }
        }
        if (!moveIsValid) {
            String invalidMoveReturn = String.format("Invalid move: %s to %s", start, end);
            invalidMoveReturn += promoPiece == null ? "" : String.format(" with promo piece %s", promoPiece.toString());
            return invalidMoveReturn;
        }
        return "opponent's turn.  Wait for their play. ";
    }

    public String resignClient(String... params){
        getListOfGamesClient();  // will reflect up to date changes in game list
        if (! this.isPlaying ){
            return "you are not playing a game and so cannot resign";
        } else if (this.hasResigned) {
            return "you already resigned";
        }
        // user should receive a prompt to confirm if they really want to resign the game or not
        String result = "";
        Scanner scanner = new Scanner(System.in);
        while (!(result.toLowerCase().equals("y") || result.toLowerCase().equals("n"))){
            System.out.println(SET_TEXT_COLOR_RED + "Do you really want to resign the game?  [Y/N]");
            String line = scanner.nextLine();
            String[] tokens = line.trim().toLowerCase().split("\\s+");
            if (tokens[0].equals("y")){
                GameData currentGame = this.gameListDisplayed.get(this.gameNumber);
                GameData updatedGame = new GameData(currentGame.gameID(),
                        currentGame.whiteUsername(),
                        currentGame.blackUsername(),
                        currentGame.gameName(),
                        currentGame.game(),
                        Boolean.FALSE);
                ConcludeGameRequest request = new ConcludeGameRequest(updatedGame.gameID());
                ConcludeGameResult resignResult = null;
                try {
                    resignResult = server.concludeGame(request);
                } catch (ResponseException e) {
                    return "failed to resign";
                }
                if (resignResult != null){
                    try {
                        ws.resignGame(this.authToken, updatedGame.gameID(), this.perspective);
                    } catch (ResponseException e) {
                        return "Failed to announce resignation";
                    }
                    getListOfGamesClient();
                this.hasResigned = Boolean.TRUE;  // updated to make it so we won't resign twice
                return "resign game";
                }
            } else if (tokens[0].equals("n")) {
                return "continuing play";
            }
        }
        return "resign game";
    }

    public void notify(String message){
        System.out.println(message);
    }

    public void notifyDrawBoard(LoadGameMessage message){
        // the client should receive and record the current game state AND print the updated board
        this.currentGame = message.getGame();
        System.out.println( "\n\n" + new DrawChess( message.getGame().game().getBoard(), this.perspective).main() );
    }

}
