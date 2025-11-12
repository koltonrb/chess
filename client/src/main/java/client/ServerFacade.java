package client;

import com.google.gson.Gson;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.LogoutResult;
import results.RegisterResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import exception.ResponseException;


public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url){ serverUrl = url; }

    private HttpRequest buildRequest(String method, String path, Object body){
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null){
            builder.setHeader("Content-Type", "application/json");
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
            throw new ResponseException( ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)){
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
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



}
