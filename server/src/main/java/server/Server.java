package server;

import io.javalin.*;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.RegisterService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public void register(){
        javalin.post("/user", ctx -> {
            UserData userRequest = ctx.bodyAsClass(UserData.class);
            RegisterService service = new RegisterService();
            AuthData result = service.register(userRequest);
            ctx.status(200).json(result);
        });
    }
}
