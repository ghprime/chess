package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
  private final String game;
  private final ChessGame.TeamColor currentTeam;

  public LoadGameMessage(ChessGame game) {
    super(ServerMessageType.LOAD_GAME);
    this.game=game.serialize();
    currentTeam=game.getTeamTurn();
  }

  public ChessGame getGame() {
    return ChessGame.deserialize(game, currentTeam);
  }

  @Override
  public String toString() {
    return "LoadGameMessage{" +
            "game='" + game + '\'' +
            ", currentTeam=" + currentTeam +
            '}';
  }
}
