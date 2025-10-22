package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import model.*;
import service.ClearService;
import service.UserService;

import java.util.HashMap;

public class Server {

    private final Javalin httpHandler;
    private final UserService userService;
    private final DataAccess dataAccess;
    private final ClearService clearService;

    public Server(){
        this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(this.dataAccess);
        this.clearService = new ClearService(this.dataAccess);

        // Register your endpoints and exception handlers here.
        this.httpHandler = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerUser)
                .post("/session", this::loginUser)
                .delete("/db", this::clearDatabase)
                .delete("/session", this::logoutUser)
                .exception(AlreadyTakenException.class, this::alreadyTakenExceptionHandler)
                .exception(BadRequestException.class, this::badRequestExceptionHandler)
                .exception(UnauthorizedException.class, this::unauthorizedExceptionHandler);
    }

//    public Server(UserService userService, DataAccess dataAccess) {
//        this.userService = userService;
//        this.dataAccess = dataAccess;
//        this.clearService = clearService;
//
//        // Register your endpoints and exception handlers here.
//        this.httpHandler = Javalin.create(config -> config.staticFiles.add("web"))
//                .post("/user", this::registerUser)
//                .exception(AlreadyTakenException.class, this::alreadyTakenExceptionHandler);
//    }

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
    // TODO add exception handler here
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

    private void clearDatabase(Context ctx) {
        ClearRequest request = new Gson().fromJson(ctx.body(), ClearRequest.class);
        ClearResult result = clearService.clear(request);
        ctx.status(200).json(new Gson().toJson( result ));
    }

}
