package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import model.ClearRequest;
import model.ClearResult;
import model.RegisterRequest;
import model.RegisterResult;
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
                .delete("/db", this::clearDatabase)
                .exception(AlreadyTakenException.class, this::alreadyTakenExceptionHandler);
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

    private void registerUser(Context ctx) throws DataAccessException {
        RegisterRequest user = new Gson().fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult userResult = userService.register( user );
        ctx.status(200).json(new Gson().toJson(userResult));
    }

    private void clearDatabase(Context ctx) {
        ClearRequest request = new Gson().fromJson(ctx.body(), ClearRequest.class);
        ClearResult result = clearService.clear(request);
        ctx.status(200).json(new Gson().toJson( result ));
    }

}
