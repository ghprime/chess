package dataaccess.sql;

import chess.ChessGame;
import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.GameDAO;
import models.GameData;

import java.util.List;

import static dataaccess.sql.SQLUtils.executeQuery;
import static dataaccess.sql.SQLUtils.executeUpdate;

public class SQLGameDAO implements GameDAO {
    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        String statement = "select * from games where id = ?";
        List<GameData> results=executeQuery(statement, gameAdapter, gameID);
        return results.isEmpty() ? null : results.getFirst();
    }

    @Override
    public int insertGameData(GameData gameData) throws DataAccessException {
        String statement="insert into games (name, game, currentTurn, whitePlayer, blackPlayer) values (?, ?, 0, ?, ?);";

        ChessGame gameToInsert=new ChessGame();
        gameToInsert.getBoard().resetBoard();

        UpdateInfo info=executeUpdate(
                statement,
                gameData.gameName(),
                gameToInsert.serialize(),
                gameData.whiteUsername(),
                gameData.blackUsername()
        );

        return info.generatedID();
    }

    @Override
    public void updateGameData(GameData gameData) throws DataAccessException {
        String updateStatement="update games set game = ?, currentTurn = ?, whitePlayer = ?, blackPlayer = ? where id = ?;";

        ChessGame chessGame = gameData.game() == null ? new ChessGame() : gameData.game();

        UpdateInfo info = executeUpdate(
                updateStatement,
                chessGame.serialize(),
                chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE ? 0 : 1,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameID()
        );

        if (info.numAffectedRows() == 0) {
            throw new DataAccessException("Cannot update a game that doesn't exist!");
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String statement = "select * from games;";

        return executeQuery(statement, gameAdapter);
    }

    @Override
    public void clear() throws DataAccessException {
        String statement="truncate games";
        executeUpdate(statement);
    }

    private final SQLAdapter<GameData> gameAdapter= rs -> new GameData(
            rs.getInt(1),
            rs.getString(5),
            rs.getString(6),
            rs.getString(2),
            ChessGame.deserialize(
                    rs.getString(3),
                    ChessGame.TeamColor.values()[rs.getInt(4)]
            )
    );
}
