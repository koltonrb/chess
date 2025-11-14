package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import requests.RegisterRequest;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port); 
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("registerPositive")
    void registerPositive() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        Assertions.assertTrue(authData.authToken().length() > 10);
    }
}
