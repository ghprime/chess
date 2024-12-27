package dataaccess.sql;

import dataaccess.*;
import dataaccess.daointerface.AuthDAO;
import dataaccess.daointerface.GameDAO;
import dataaccess.daointerface.UserDAO;
import dataaccess.exception.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLDAOManager implements DAOManager {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    private static SQLDAOManager instance = null;

    public static DAOManager getInstance() {
        return instance;
    }

    public SQLDAOManager() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (var conn=SQLUtils.getConnection()) {
            for (String statement : createStatements) {
                try (PreparedStatement preparedStatement=conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }

        userDAO = new SQLUserDAO();
        authDAO = new SQLAuthDAO();
        gameDAO = new SQLGameDAO();

        if (instance == null) {
            instance = this;
        }
    }

    private final String[] createStatements={
            """
          create table if not exists games (
            id int not null auto_increment,
            name varchar(256) not null,
            game char(64) not null,
            currentTurn int not null,
            whitePlayer varchar(256),
            blackPlayer varchar(256),
            gameOver boolean not null,
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

    @Override
    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    @Override
    public GameDAO getGameDAO() {
        return gameDAO;
    }
}
