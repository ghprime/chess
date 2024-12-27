package service.server;

import chess.ChessGame;
import dataaccess.exception.AlreadyTakenException;
import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.GameDAO;
import models.AuthData;
import models.GameData;

public class JoinGameService {
    private final GameDAO gameDAO;

    public JoinGameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void joinGame(int gameID, ChessGame.TeamColor playerColor, AuthData authData) throws DataAccessException {
        AuthData verifiedAuth = ServiceUtils.validateAuthToken(authData);

        GameData gameToJoin = gameDAO.getGameData(gameID);

        if (gameToJoin == null) {
            throw new DataAccessException(String.format("No game with game id %d!", gameID));
        }

        GameData updatedGame;

        if (playerColor == ChessGame.TeamColor.BLACK) {
            if (gameToJoin.blackUsername() != null) {
                throw new AlreadyTakenException("Player BLACK is already filled!");
            }
            updatedGame = new GameData(
                    gameID,
                    gameToJoin.whiteUsername(),
                    verifiedAuth.username(),
                    gameToJoin.gameName(),
                    gameToJoin.game(),
                    gameToJoin.gameOver()
            );
        } else {
            if (gameToJoin.whiteUsername() != null) {
                throw new AlreadyTakenException("Player WHITE is already filled!");
            }
            updatedGame = new GameData(
                    gameID,
                    verifiedAuth.username(),
                    gameToJoin.blackUsername(),
                    gameToJoin.gameName(),
                    gameToJoin.game(),
                    gameToJoin.gameOver()
            );
        }

        gameDAO.updateGameData(updatedGame);
    }
}
