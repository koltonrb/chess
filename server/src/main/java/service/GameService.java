package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
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
}
