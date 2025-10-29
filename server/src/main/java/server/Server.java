package server;

import exception.AlreadyTakenException;
import exception.BadRequestException;
import exception.DataAccessException;
import exception.UnauthorizedException;
import requests.*;
import results.*;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.ClearService;
import service.GameService;
import service.UserService;

import java.util.HashMap;

public class Server {

    private final Javalin httpHandler;
    private final UserService userService;
    private final DataAccess dataAccess;
    private final GameService gameService;
    private final ClearService clearService;

    public Server(){
//        this.dataAccess = new MemoryDataAccess();
        this.dataAccess = new MySqlDataAccess();
        this.userService = new UserService(this.dataAccess);
        this.gameService = new GameService(this.dataAccess);
        this.clearService = new ClearService(this.dataAccess);

        // Register your endpoints and exception handlers here.
        this.httpHandler = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerUser)
                .post("/session", this::loginUser)
                .delete("/db", this::clearDatabase)
                .delete("/session", this::logoutUser)
                .post("/game", this::createGame)
                .get("/game", this::listGames)
                .put("/game", this::joinGame)
                .exception(AlreadyTakenException.class, this::alreadyTakenExceptionHandler)
                .exception(BadRequestException.class, this::badRequestExceptionHandler)
                .exception(UnauthorizedException.class, this::unauthorizedExceptionHandler);
    }

    public int run(int desiredPort) {
        httpHandler.start(desiredPort);
        return httpHandler.port();  // pet shop returns 'this'?
    }

    public int port() { return httpHandler.port(); }

    public void stop() {
        httpHandler.stop();
    }

    private String exceptionToJSON(Exception ex){
        HashMap<String, String> errorHash = new HashMap<>();
        errorHash.put("message", "Error: " + ex.getMessage() );
        return new Gson().toJson(errorHash);
    }

    private void alreadyTakenExceptionHandler(AlreadyTakenException ex, Context ctx){
        ctx.status(403);
        ctx.result(exceptionToJSON(ex));
    }

    private void badRequestExceptionHandler(BadRequestException ex, Context ctx){
        ctx.status(400);
        ctx.result(exceptionToJSON(ex));
    }

    private void unauthorizedExceptionHandler(UnauthorizedException ex, Context ctx){
        ctx.status(401);
        ctx.result(exceptionToJSON(ex));
    }

    private void registerUser(Context ctx) throws BadRequestException, AlreadyTakenException,
            DataAccessException {
        RegisterRequest user = new Gson().fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult userResult = userService.register( user );
        ctx.status(200).json(new Gson().toJson(userResult));
    }

    private void loginUser(Context ctx) throws BadRequestException, AlreadyTakenException,
            DataAccessException {
        LoginRequest user = new Gson().fromJson(ctx.body(), LoginRequest.class);
        LoginResult userResult = userService.login( user );
        ctx.status(200).json(new Gson().toJson(userResult));

    }

    private void logoutUser(Context ctx) throws UnauthorizedException, DataAccessException {
        String authToken = ctx.header("authorization");
        LogoutRequest user = new Gson().fromJson(ctx.body(), LogoutRequest.class);
        LogoutResult userResult = userService.logout( user, authToken );
        ctx.status(200).json(new Gson().toJson(userResult));
    }

    private void createGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        CreateGameRequest request = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
        CreateGameResult result = gameService.createGame( request, authToken);
        ctx.status(200).json(new Gson().toJson( result ));

    }

    private void listGames(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        ListGamesRequest request = new Gson().fromJson(ctx.body(), ListGamesRequest.class);
        ListGamesResult result = gameService.listGames( request, authToken );
        ctx.status(200).json(new Gson().toJson( result ));
    }

    private void joinGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        JoinGameRequest request = new Gson().fromJson(ctx.body(), JoinGameRequest.class);
        JoinGameResult result = gameService.joinGame( request, authToken);
        ctx.status(200).json(new Gson().toJson( result));
    }

    private void clearDatabase(Context ctx) {
        ClearRequest request = new Gson().fromJson(ctx.body(), ClearRequest.class);
        ClearResult result = clearService.clear(request);
        ctx.status(200).json(new Gson().toJson( result ));
    }

}
