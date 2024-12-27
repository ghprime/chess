package websocket.commands;

import chess.ChessGame;

public class ConnectPlayerCommand extends UserGameCommand {
  private final ChessGame.TeamColor playerColor;

  public ConnectPlayerCommand(String authToken, int gameID, ChessGame.TeamColor playerColor) {
    super(CommandType.CONNECT, authToken, gameID);
    this.playerColor = playerColor;
  }

  public ChessGame.TeamColor getPlayerColor() {
    return playerColor;
  }
}
