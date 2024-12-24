package models;

public record GameInfo(int gameID, String whiteUsername, String blackUsername, String gameName) {
  static public GameInfo fromGame(GameData gameData) {
    return new GameInfo(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName());
  }
  public GameData toGame() {
    return new GameData(gameID, whiteUsername, blackUsername, gameName, null);
  }
}
