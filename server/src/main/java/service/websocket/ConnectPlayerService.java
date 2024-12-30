package service.websocket;

import chess.ChessGame;
import dataaccess.daointerface.GameDAO;
import dataaccess.exception.BadRequestException;
import dataaccess.exception.DataAccessException;
import models.AuthData;
import models.GameData;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import websocket.commands.ConnectPlayerCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class ConnectPlayerService {
    private final ConnectionManager conns;
    private final GameDAO gameDAO;

    public ConnectPlayerService(ConnectionManager conns, GameDAO gameDAO) {
        this.conns = conns;
        this.gameDAO = gameDAO;
    }

    public void connectPlayer(Session session, ConnectPlayerCommand command) throws DataAccessException {
        GameData game = gameDAO.getGameData(command.getGameID());

        if (command.getPlayerColor() == null) {
            throw new BadRequestException();
        }

        AuthData authData = this.conns.addConnection(command, session);

        String color;
        String existingPlayer;

        if (command.getPlayerColor() == ChessGame.TeamColor.WHITE) {
            existingPlayer = game.whiteUsername();
            color = "WHITE";
        } else if (command.getPlayerColor() == ChessGame.TeamColor.BLACK) {
            existingPlayer = game.blackUsername();
            color = "BLACK";
        } else {
            throw new DataAccessException("Invalid player color!");
        }

        if (existingPlayer == null) {
            throw new DataAccessException("Must join before connecting!");
        }

        if (!authData.username().equals(existingPlayer)) {
            throw new DataAccessException(String.format("Cannot join: %s already filled!", color));
        }

        this.conns.broadcastMessage(command, new NotificationMessage(
                String.format(
                        "%s has connected as the %s player",
                        authData.username(),
                        color)
                )
        );

        this.conns.sendMessage(command, new LoadGameMessage(game.game()));
    }
}
