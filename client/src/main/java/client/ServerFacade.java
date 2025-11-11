package client;

import com.google.gson.Gson;
import requests.RegisterRequest;
import results.RegisterResult;

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

    private HttpResponse<String> sendRequest(HttpRequest request){
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException( ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public RegisterResult registerUser(RegisterRequest request){

    }



}
