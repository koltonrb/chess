package client;

import com.google.gson.Gson;
import requests.*;
import results.*;

import java.lang.module.ResolutionException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;


public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private String authToken;

    public ServerFacade(int port){
        String urlBase = "http://localhost:";
        serverUrl = urlBase + String.format("%d", port);
        authToken = null;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private HttpRequest buildRequest(String method, String path, Object body){
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null){
            builder.setHeader("Content-Type", "application/json");
        }
        if (authToken != null) {
            builder.setHeader("authorization", this.authToken);
        }
        HttpRequest request = builder.build();
        return request;
    }

    private BodyPublisher makeRequestBody(Object request){
        if (request != null){
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException( ResponseException.Code.OtherServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)){
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body, status);
            }
            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }
        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean isSuccessful(int status) {return status / 100 == 2; }

    public RegisterResult registerUser(RegisterRequest user) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/user", user);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }

    public LoginResult loginUser(LoginRequest user) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/session", user);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, LoginResult.class);
    }


    public LogoutResult logoutUser(LogoutRequest user) throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/session", user);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, LogoutResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest userInput) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/game", userInput);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, CreateGameResult.class);
    }

    public ListGamesResult listGames(ListGamesRequest userInput) throws ResponseException {
        HttpRequest request = buildRequest("GET", "/game", userInput);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, ListGamesResult.class);
    }

    public JoinGameResult joinGame(JoinGameRequest userInput) throws ResponseException {
        HttpRequest request = buildRequest("PUT", "/game", userInput);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, JoinGameResult.class);
    }

    public ClearResult clearDB(ClearRequest userInput) throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/db", userInput);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, ClearResult.class);
    }

    public UpdateGameResult updateGame(UpdateGameRequest userInput ) throws ResponseException {
        HttpRequest request = buildRequest("PATCH", "/game", userInput);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, UpdateGameResult.class);
    }

    public ConcludeGameResult concludeGame(ConcludeGameRequest userInput) throws ResponseException{
        HttpRequest request = buildRequest("PATCH", "/gameover", userInput);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, ConcludeGameResult.class);
    }

    public MakeMoveResult makeMove(MakeMoveRequest userInput) throws ResponseException{
        HttpRequest request = buildRequest("PATCH", "/gamemove", userInput);
        HttpResponse<String> response = sendRequest( request );
        return handleResponse( response, MakeMoveResult.class);
    }

}
