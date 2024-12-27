package dataaccess.memory;

import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.GameDAO;
import models.GameData;

import java.util.HashMap;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int currGameID = 0;

    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public int insertGameData(GameData gameData) throws DataAccessException {
        GameData gameToInsert = new GameData(
                ++currGameID,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                gameData.game(),
                gameData.gameOver()
        );
        
        games.put(currGameID, gameToInsert);

        return currGameID;
    }

    @Override
    public void updateGameData(GameData gameData) throws DataAccessException {
        if (games.get(gameData.gameID()) == null) {
            throw new DataAccessException("No such game!");
        }
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return games.values().stream().toList();
    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
    }
}
