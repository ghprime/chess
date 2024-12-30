package serverfacade;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import models.AuthData;
import ui.ChessClient;
import ui.ClientException;
import websocket.commands.UserGameCommand;
import websocket.commands.UserMessageFactory;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebsocketFacade extends Endpoint {
  Session session;
  NotificationHandler notificationHandler;
  UserMessageFactory messageFactory;

  public WebsocketFacade(String url, NotificationHandler notificationHandler, AuthData authData, int gameID, ChessClient client) throws ClientException {
    try {
      url=url.replace("http", "ws");
      var socketURI=new URI(url + "/ws");
      this.messageFactory=new UserMessageFactory(authData, gameID);

      var container=ContainerProvider.getWebSocketContainer();
      this.session=container.connectToServer(this, socketURI);
      this.notificationHandler=notificationHandler;

      session.addMessageHandler(new MessageHandler.Whole<String>() {
        @Override
        public void onMessage(String message) {
          var serverMessage=new Gson().fromJson(message, ServerMessage.class);
          switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> {
              var gameMessage=fromJson(message, LoadGameMessage.class);
              notificationHandler.notify(client.displayGame(gameMessage.getGame()));
            }
            case ERROR -> {
              var error=fromJson(message, ErrorMessage.class);
              notificationHandler.error(error.getErrorMessage());
            }
            case NOTIFICATION -> {
              var notification=fromJson(message, NotificationMessage.class);
              notificationHandler.notify(notification.getMessage());
            }
          }
        }
      });

    } catch (URISyntaxException | DeploymentException | IOException e) {
      throw new ClientException(500, e.getMessage());
    }
  }

  private <T> T fromJson(String json, Class<T> type) {
    return new Gson().fromJson(json, type);
  }

  private String toJson(Object o) {
    return new Gson().toJson(o);
  }

  private void sendMessage(UserGameCommand message) throws ClientException {
    try {
      session.getBasicRemote().sendText(toJson(message));
    } catch (IOException e) {
      throw new ClientException(400, e.getMessage());
    }
  }

  public void close() throws ClientException {
    try {
      session.close();
    } catch (IOException e) {
      throw new ClientException(e.getMessage());
    }
  }

  public void joinPlayer(ChessGame.TeamColor teamColor) throws ClientException {
    sendMessage(messageFactory.joinPlayer(teamColor));
  }

  public void joinObserver() throws ClientException {
    sendMessage(messageFactory.joinObserver());
  }

  public void makeMove(ChessMove move) throws ClientException {
    sendMessage(messageFactory.makeMove(move));
  }

  public void leave() throws ClientException {
    sendMessage(messageFactory.leave());
  }

  public void resign() throws ClientException {
    sendMessage(messageFactory.resign());
  }

  @Override
  public void onOpen(Session session, EndpointConfig endpointConfig) {
  }
}
