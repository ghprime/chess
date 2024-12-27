package models;

import chess.ChessGame;

/**
 * Contains game information. Can be incomplete, i.e. contain only game ID for database lookups
 *
 * @param gameID        ID of a game
 * @param whiteUsername Username of the white player
 * @param blackUsername Username of the black player
 * @param gameName      Name of the game
 * @param game          ChessGame object
 */
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game, boolean gameOver) {
  /**
   * Creates a game record with only the game ID. Used for database lookups
   *
   * @param gameID Integer ID of the game
   */
  public GameData(int gameID) {
    this(gameID, "", "", "", null, false);
  }

  /**
   * Creates a game record with only the game name. Used for quick game creation
   *
   * @param gameName String name of the game to create
   */
  public GameData(String gameName) {
    this(0, null, null, gameName, null, false);
  }

  public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    this(gameID, whiteUsername, blackUsername, gameName, game, false);
  }
}
