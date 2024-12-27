package dataaccess.sql;

import dataaccess.exception.DataAccessException;
import dataaccess.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

class SQLUtils {
    static UpdateInfo executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn=getConnection()) {
            try (var ps=conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                insertParametersIntoPreparedStatement(ps, params);
                var numAffectedRows=ps.executeUpdate();
                var generatedID=0;
                var rs=ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedID=rs.getInt(1);
                }
                return new UpdateInfo(numAffectedRows, generatedID);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
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
    static <T> List<T> executeQuery(String statement, SQLAdapter<T> adapter, Object... params) throws DataAccessException {
        try (var conn=getConnection()) {
            try (var ps=conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                insertParametersIntoPreparedStatement(ps, params);

                return getResultsFromQuery(ps, adapter);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    static Connection getConnection() throws DataAccessException {
        return DatabaseManager.getConnection();
    }

    private static <T> List<T> getResultsFromQuery(PreparedStatement ps, SQLAdapter<T> adapter) throws SQLException {
        var results=new ArrayList<T>();
        try (var rs=ps.executeQuery()) {
            while (rs.next()) {
                results.add(adapter.getClass(rs));
            }
        }
        return results;
    }

    private static void insertParametersIntoPreparedStatement(PreparedStatement ps, Object... params) throws SQLException {
        for (var i=0; i < params.length; i++) {
            var param=params[i];

            switch (param) {
                case String s -> ps.setString(i + 1, s);
                case Integer integer -> ps.setInt(i + 1, integer);
                case Boolean bool -> ps.setBoolean(i + 1, bool);
                case null -> ps.setNull(i + 1, NULL);
                default -> {
                }
            }
        }
    }
}
