package dataaccess.sql;

import dataaccess.exception.AlreadyTakenException;
import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.UserDAO;
import models.UserData;

import java.util.List;

import static dataaccess.sql.SQLUtils.executeQuery;
import static dataaccess.sql.SQLUtils.executeUpdate;

public class SQLUserDAO implements UserDAO {
    @Override
    public UserData getUser(String username) throws DataAccessException {
        String getUserStatement="select * from users where username=?;";
        SQLAdapter<UserData> userAdapter= rs -> new UserData(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3)
        );

        List<UserData> results=executeQuery(getUserStatement, userAdapter, username);

        return results.isEmpty() ? null : results.getFirst();
    }

    @Override
    public void insertUser(UserData userData) throws DataAccessException {
        String statement="insert into users values(?,?,?)";
        try {
            executeUpdate(statement, userData.username(), userData.password(), userData.email());
        } catch (DataAccessException e) {
            throw new AlreadyTakenException("Username already taken!");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement="truncate users";
        executeUpdate(statement);
    }
}
