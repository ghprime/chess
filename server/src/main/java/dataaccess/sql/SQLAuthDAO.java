package dataaccess.sql;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.UnauthorizedException;
import models.AuthData;

import java.util.List;

import static dataaccess.sql.SQLUtils.executeQuery;
import static dataaccess.sql.SQLUtils.executeUpdate;

public class SQLAuthDAO implements AuthDAO {
    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        String statement="select username from authTokens where authToken=?;";
        SQLAdapter<AuthData> tokenAdapter= rs -> new AuthData(authToken, rs.getString(1));
        List<AuthData> results=executeQuery(statement, tokenAdapter, authToken);
        return results.isEmpty() ? null : results.getFirst();
    }

    @Override
    public void insertAuthData(AuthData authData) throws DataAccessException {
        String insertAuthTokenStatement="insert into authTokens values(?,?)";
        executeUpdate(insertAuthTokenStatement, authData.username(), authData.authToken());
    }

    @Override
    public void deleteAuthData(String authToken) throws DataAccessException {
        String statement="delete from authTokens where authToken=?;";

        executeUpdate(statement, authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        String statement="truncate authTokens";
        executeUpdate(statement);
    }
}
