package service.websocket;

import dataaccess.exception.BadRequestException;
import dataaccess.exception.DataAccessException;
import models.AuthData;
import models.GameData;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import websocket.commands.ConnectObserverCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import static service.websocket.WebsocketServiceUtils.getGame;
import static service.websocket.WebsocketServiceUtils.validateAuthToken;

public class ConnectObserverService {
    private final ConnectionManager conns;

    public ConnectObserverService(ConnectionManager conns) {
        this.conns = conns;
    }

    public void connectObserver(Session session, ConnectObserverCommand command) throws DataAccessException {
        AuthData authData = validateAuthToken(command.getAuthToken());

        GameData game = getGame(command.getGameID());

        if (game == null) {
            throw new BadRequestException();
        }

        this.conns.addConnection(command, session);

        this.conns.broadcastMessage(command, new NotificationMessage(String.format("%s is now observing the game", authData.username())));

        this.conns.sendMessage(command, new LoadGameMessage(game.game()));
    }
}
