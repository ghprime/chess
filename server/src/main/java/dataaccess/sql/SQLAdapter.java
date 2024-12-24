package dataaccess.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

interface SQLAdapter<T> {
    T getClass(ResultSet rs) throws SQLException;
}
