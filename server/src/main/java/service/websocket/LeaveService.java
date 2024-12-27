package service.websocket;

import dataaccess.daointerface.GameDAO;
import dataaccess.exception.DataAccessException;
import models.AuthData;
import models.GameData;
import server.websocket.ConnectionManager;
import websocket.commands.LeaveCommand;
import websocket.messages.NotificationMessage;

import static service.websocket.WebsocketServiceUtils.validateAuthToken;

public class LeaveService {
    private final ConnectionManager conns;
    private final GameDAO gameDAO;

    public LeaveService(ConnectionManager conns, GameDAO gameDAO) {
        this.conns = conns;
        this.gameDAO = gameDAO;
    }

    public void leave(LeaveCommand command) throws DataAccessException {
        AuthData authData = validateAuthToken(command.getAuthToken());

        GameData game = gameDAO.getGameData(command.getGameID());

        GameData updatedGame;
        String color;

        if (authData.username().equals(game.whiteUsername())) {
            updatedGame = new GameData(
                    game.gameID(),
                    null,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );

            color = "WHITE";
        } else if (authData.username().equals(game.blackUsername())) {
            updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    null,
                    game.gameName(),
                    game.game()
            );

            color = "BLACK";
        } else {
            conns.broadcastMessage(command, new NotificationMessage(
                    String.format(
                            "%s (observer) has left the game",
                            authData.username())
                    )
            );
            conns.removeConnection(command);
            return;
        }

        gameDAO.updateGameData(updatedGame);

        conns.broadcastMessage(command, new NotificationMessage(
                String.format(
                        "%s (%s) has left the game",
                        authData.username(),
                        color
                )
        ));

        conns.removeConnection(command);
    }
}
