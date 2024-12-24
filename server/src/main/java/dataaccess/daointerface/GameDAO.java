package dataaccess.daointerface;

import dataaccess.exception.DataAccessException;
import models.GameData;

import java.util.List;

public interface GameDAO {
    GameData getGameData(int gameID) throws DataAccessException;
    /**
     * @param gameData Doesn't contain gameID
     * @return integer gameID
     * @throws DataAccessException
     */
    int insertGameData(GameData gameData) throws DataAccessException;
    /**
     * @param gameData Must contain gameID
     * @throws DataAccessException
     */
    void updateGameData(GameData gameData) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void clear() throws DataAccessException;
}
