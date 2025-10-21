package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import model.RegisterRequest;
import model.RegisterResult;
import service.UserService;

public class Server {

    private final Javalin httpHandler;
    private final UserService userService;

    public Server(UserService userService) {
        this.userService = userService;

        // Register your endpoints and exception handlers here.
        this.httpHandler = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerUser);
//                .exception(ResponseException.class, this::exceptionHander);
    }

    public int run(int desiredPort) {
        httpHandler.start(desiredPort);
        return httpHandler.port();  // pet shop returns 'this'?
    }

    public int port() { return httpHandler.port(); }

    public void stop() {
        httpHandler.stop();
    }

    // TODO add exception handler here

    private void registerUser(Context ctx) {
        RegisterRequest user = new Gson().fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult userResult = userService.register( user );
        ctx.json(new Gson().toJson(userResult));
    }

//    public void register(){
//        javalin.post("/user", ctx -> {
//            UserData userRequest = ctx.bodyAsClass(UserData.class);
//            UserService service = new UserService();
//            AuthData result = service.register(userRequest);
//            ctx.status(200).json(result);
//        });
//    }
}
