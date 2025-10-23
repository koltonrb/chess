package service;

import dataaccess.*;
import model.*;

import java.util.ArrayList;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess){ this.dataAccess = dataAccess; }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest, String authToken) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null){
            throw new UnauthorizedException("unauthorized");
        }
        ArrayList<GameData> games = dataAccess.listGames();
        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null){
            throw new UnauthorizedException("unauthorized");
        }
        if (request.gameName() == null){
            throw new BadRequestException("Bad Request");
        }
        GameData gameData = dataAccess.createGame(request.gameName());
        return new CreateGameResult(gameData.gameID());
    }

    public JoinGameResult joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        ArrayList<String> acceptableColors = new ArrayList<>();
        acceptableColors.add("WHITE");
        acceptableColors.add("BLACK");

        if ((!acceptableColors.contains(request.playerColor()))
                || (!dataAccess.getGames().containsKey(request.gameID()))){
            // checks if team is WHITE or BLACK AND that the gameID exists
            throw new BadRequestException("Bad Request");
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null){
            throw new UnauthorizedException("unauthorized");
        }

        GameData game = dataAccess.getGames().get(request.gameID());
        if (((request.playerColor().equals("WHITE")) && (game.whiteUsername() != null))
            || ((request.playerColor().equals("BLACK")) && (game.blackUsername() != null))){
            throw new AlreadyTakenException("there is already someone playing that color");
        }
        GameData updatedGame;
        if (request.playerColor().equals("WHITE")){
            updatedGame = new GameData(game.gameID(),
                authData.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game());

        } else {
            updatedGame = new GameData(game.gameID(),
                    game.whiteUsername(),
                    authData.username(),
                    game.gameName(),
                    game.game());
        }
        dataAccess.updateGame( updatedGame );
        return new JoinGameResult();


    }
}
