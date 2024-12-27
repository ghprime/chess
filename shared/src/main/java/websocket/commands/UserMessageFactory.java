package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;
import models.AuthData;

public class UserMessageFactory {
  AuthData authData;
  int gameID;

  public UserMessageFactory(AuthData authData, int gameID) {
    this.authData=authData;
    this.gameID = gameID;
  }

  public ConnectObserverCommand joinObserver() {
    return new ConnectObserverCommand(authData.authToken(), gameID);
  }

  public ConnectPlayerCommand joinPlayer(ChessGame.TeamColor teamColor) {
    return new ConnectPlayerCommand(authData.authToken(), gameID, teamColor);
  }

  public MakeMoveCommand makeMove(ChessMove move) {
    return new MakeMoveCommand(authData.authToken(), move, gameID);
  }

  public LeaveCommand leave() {
    return new LeaveCommand(authData.authToken(), gameID);
  }

  public ResignCommand resign() {
    return new ResignCommand(authData.authToken(), gameID);
  }
}
