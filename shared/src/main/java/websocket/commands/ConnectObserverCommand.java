package websocket.commands;

public class ConnectObserverCommand extends UserGameCommand {
  public ConnectObserverCommand(String authToken, int gameID) {
    super(CommandType.CONNECT, authToken, gameID);
  }
}
