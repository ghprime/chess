package dataaccess;

import chess.ChessGame;
import models.AuthToken;
import models.Game;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDAO implements DatabaseAccess {
  private static MySqlDAO instance;
  /**
   * Statements to create database and tables if they don't already exist
   */
  private final String[] createStatements={
          """
          create table if not exists games (
            id int not null auto_increment,
            name varchar(256) not null,
            game char(64) not null,
            currentTurn int not null,
            whitePlayer varchar(256),
            blackPlayer varchar(256),
            primary key (id)
          );
          """,
          """
          create table if not exists users (
            username varchar(256) not null unique,
            password varchar(256) not null,
            email varchar(256) not null,
            primary key (username)
          );
          """,
          """
          create table if not exists authTokens (
            username varchar(256) not null,
            authToken char(36) not null,
            primary key (authToken),
            index(username)
          );
          """
  };

  private final Adapter<Game> gameAdapter=rs -> new Game(
          rs.getInt(1),
          rs.getString(5),
          rs.getString(6),
          rs.getString(2),
          ChessGame.deserialize(
                  rs.getString(3),
                  ChessGame.TeamColor.values()[rs.getInt(4)]
          )
  );

  public MySqlDAO() throws DataAccessException {
    start();
  }

  public static MySqlDAO getInstance() throws DataAccessException {
    if (instance != null) {
      return instance;
    }

    try {
      instance=new MySqlDAO();
    } catch (Exception ex) {
      throw new DataAccessException(ex.getMessage());
    }

    return instance;
  }

  /**
   * Called to start the database with the config supplied
   */
  private void start() throws DataAccessException {
    DatabaseManager.createDatabase();

    try (var conn=getConnection()) {
      for (var statement : createStatements) {
        try (var preparedStatement=conn.prepareStatement(statement)) {
          preparedStatement.executeUpdate();
        }
      }
    } catch (SQLException ex) {
      throw new DataAccessException(ex.getMessage());
    }
  }

  /**
   * Get a connection to the database
   *
   * @return new connection
   */
  private Connection getConnection() throws DataAccessException {
    return DatabaseManager.getConnection();
  }

  /**
   * Execute a query to the database
   *
   * @param statement Query to execute
   * @param adapter   Each result will be served to this adapter, which can convert the supplied rows from the set into objects
   * @param params    Replace each '?' in the query with a param in the same order they are presented
   * @param <T>       The type of object to be returned by the query after being adapted
   * @return ArrayList of whatever the adapter returns
   */
  private <T> ArrayList<T> executeQuery(String statement, Adapter<T> adapter, Object... params) throws DataAccessException {
    try (var conn=getConnection()) {
      try (var ps=conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
        insertParametersIntoPreparedStatement(ps, params);

        return getResultsFromQuery(ps, adapter);
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  private <T> ArrayList<T> getResultsFromQuery(PreparedStatement ps, Adapter<T> adapter) throws SQLException {
    var results=new ArrayList<T>();
    try (var rs=ps.executeQuery()) {
      while (rs.next()) {
        results.add(adapter.getClass(rs));
      }
    }
    return results;
  }

  private void insertParametersIntoPreparedStatement(PreparedStatement ps, Object... params) throws SQLException {
    for (var i=0; i < params.length; i++) {
      var param=params[i];

      switch (param) {
        case String s -> ps.setString(i + 1, s);
        case Integer integer -> ps.setInt(i + 1, integer);
        case null -> ps.setNull(i + 1, NULL);
        default -> {
        }
      }
    }
  }

  /**
   * Used to run update statements, like insert, update, and delete
   *
   * @param statement Statement to execute
   * @param params    Parameters will replace the '?'s in the order they appear in the statement
   * @return A tuple of how many rows were updated, and a generated key if one was created
   */
  private Tuple executeUpdate(String statement, Object... params) throws DataAccessException {
    try (var conn=getConnection()) {
      try (var ps=conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
        insertParametersIntoPreparedStatement(ps, params);
        var numAffectedRows=ps.executeUpdate();
        var generatedID=0;
        var rs=ps.getGeneratedKeys();
        if (rs.next()) {
          generatedID=rs.getInt(1);
        }
        return new Tuple(numAffectedRows, generatedID);
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  /**
   * Provided an auth token, return either a full auth token or null if the auth token doesn't exist (aka not authenticated)
   *
   * @param authToken Auth token to authenticate
   * @return A full authtoken wth authtoken and username, or null if unauthenticated
   */
  private AuthToken authenticatedUser(AuthToken authToken) throws DataAccessException {
    try (var conn=getConnection()) {
      var statement="select username from authTokens where authToken=?;";
      Adapter<AuthToken> tokenAdapter=rs -> new AuthToken(authToken.authToken(), rs.getString(1));
      var results=executeQuery(statement, tokenAdapter, authToken.authToken());
      return results.isEmpty() ? null : results.getFirst();
    } catch (SQLException ex) {
      throw new DataAccessException(ex.getMessage());
    }
  }

  @Override
  public void clear() throws DataAccessException {
    var tables=new String[]{"games", "users", "authTokens"};
    for (var table : tables) {
      var statement="truncate " + table;
      executeUpdate(statement);
    }
  }

  private String encryptPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  private boolean verifyPassword(String password, String encryptedPassword) {
    return BCrypt.checkpw(password, encryptedPassword);
  }

  @Override
  public AuthToken insertUser(User newUser) throws DataAccessException {
    if (newUser == null) {
      throw new DataAccessException("bad request");
    }
    if (newUser.password() == null || newUser.username() == null || newUser.email() == null) {
      throw new DataAccessException("bad request");
    }

    var statement="insert into users values(?,?,?)";
    try {
      executeUpdate(statement, newUser.username(), encryptPassword(newUser.password()), newUser.email());
    } catch (DataAccessException ex) {
      var isDuplicate=ex.getMessage().equals("Duplicate entry '" + newUser.username() + "' for key 'users.PRIMARY'");
      if (isDuplicate) {
        throw new DataAccessException("already taken");
      }
      throw ex;
    }

    return loginUser(newUser);
  }

  @Override
  public AuthToken loginUser(User user) throws DataAccessException {
    if (user == null) {
      throw new DataAccessException("bad request");
    }

    class Tuple<X, Y> {
      private final X x;
      private final Y y;
      public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
      }

      public X getX() {
        return x;
      }

      public Y getY() {
        return y;
      }
    }

    var getUserStatement="select username, password from users where username=?;";
    Adapter<Tuple<AuthToken, String>> userAdapter=rs -> new Tuple<>(
      new AuthToken(rs.getString(1)), rs.getString(2)
    );

    var results=executeQuery(getUserStatement, userAdapter, user.username());
    if (results.isEmpty()) {
      throw new DataAccessException("unauthorized");
    }

    var tuple=results.getFirst();
    var authToken=tuple.getX();
    var password=tuple.getY();

    if (!verifyPassword(user.password(), password)) {
      throw new DataAccessException("unauthorized");
    }

    var insertAuthTokenStatement="insert into authTokens values(?,?)";
    executeUpdate(insertAuthTokenStatement, authToken.username(), authToken.authToken());
    return authToken;
  }

  @Override
  public void logoutUser(AuthToken authToken) throws DataAccessException {
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }

    var statement="delete from authTokens where authToken=?;";

    var tuple=executeUpdate(statement, authToken.authToken());
    if (tuple.numAffectedRows() == 0) {
      throw new DataAccessException("unauthorized");
    }
  }

  @Override
  public List<Game> listGames(AuthToken authToken) throws DataAccessException {
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }
    authToken=authenticatedUser(authToken);
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }

    var statement="select * from games;";

    return executeQuery(statement, gameAdapter);
  }

  @Override
  public Game createGame(AuthToken authToken, Game game) throws DataAccessException {
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }
    if (game == null) {
      throw new DataAccessException("bad request");
    }

    authToken=authenticatedUser(authToken);
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }

    var statement="insert into games (name, game, currentTurn, whitePlayer, blackPlayer) values (?, ?, 0, ?, ?);";

    var gameToInsert=new ChessGame();
    gameToInsert.getBoard().resetBoard();

    var tuple=executeUpdate(statement, game.gameName(), gameToInsert.serialize(), game.whiteUsername(), game.blackUsername());

    return new Game(tuple.generatedID(), game.whiteUsername(), game.whiteUsername(), game.gameName(), new ChessGame());
  }

  @Override
  public void joinGame(AuthToken authToken, Game game) throws DataAccessException {
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }
    if (game == null) {
      throw new DataAccessException("bad request");
    }

    authToken=authenticatedUser(authToken);

    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }

    var username=authToken.username();

    var statement="select * from games where id=?;";
    var results=executeQuery(statement, gameAdapter, game.gameID());

    if (results.isEmpty()) {
      throw new DataAccessException("bad request");
    }

    var gameToJoin=results.getFirst();

    if (game.blackUsername() == null && game.whiteUsername() == null) {
      return;
    }

    var whiteUsername=gameToJoin.whiteUsername();
    var blackUsername=gameToJoin.blackUsername();
    var nullUserToReplace=game.blackUsername() != null ? blackUsername : whiteUsername;

    if (nullUserToReplace != null && !nullUserToReplace.isEmpty()) {
      throw new DataAccessException("already taken");
    }

    if (game.whiteUsername() == null || game.whiteUsername().isEmpty()) {
      blackUsername=username;
    }
    else {
      whiteUsername=username;
    }

    var updateStatement="update games set whitePlayer = ?, blackPlayer = ? where id = ?;";

    executeUpdate(updateStatement, whiteUsername, blackUsername, game.gameID());
  }

  @Override
  public AuthToken verifyAuthToken(AuthToken authToken) throws DataAccessException {
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }
    return authenticatedUser(authToken);
  }

  @Override
  public void updateGame(AuthToken authToken, Game game) throws DataAccessException {
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }
    if (game == null) {
      throw new DataAccessException("bad request");
    }
    authToken=authenticatedUser(authToken);
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }
    var gameID=game.gameID();

    var statement="select * from games where id=?;";
    var results=executeQuery(statement, gameAdapter, gameID);
    if (results.isEmpty()) {
      throw new DataAccessException("No game");
    }

    var statement1="update games set game = ?, currentTurn = ? where id = ?;";
    var trueGame=(ChessGame) game.game();
    var currentTurn=trueGame.getTeamTurn() == ChessGame.TeamColor.WHITE ? 0 : 1;
    executeUpdate(statement1, trueGame.serialize(), currentTurn, game.gameID());
  }

  @Override
  public Game getGame(AuthToken authToken, int gameID) throws DataAccessException {
    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }
    authToken=authenticatedUser(authToken);

    if (authToken == null) {
      throw new DataAccessException("unauthorized");
    }

    var statement="select * from games where id=?;";
    var results=executeQuery(statement, gameAdapter, gameID);
    if (results.isEmpty()) {
      throw new DataAccessException("No game");
    }
    return results.getFirst();
  }

  private interface Adapter<T> {
    T getClass(ResultSet rs) throws SQLException;
  }

  private record Tuple(int numAffectedRows, int generatedID) {
  }
}
