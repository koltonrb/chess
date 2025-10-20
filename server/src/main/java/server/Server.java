package server;

import io.javalin.*;
import model.AuthData;
import model.UserData;
import service.UserService;

public class Server {

    private final Javalin javalin;
    private final UserService userService;

    public Server(UserService userService) {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        this.userService = userService;

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
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
