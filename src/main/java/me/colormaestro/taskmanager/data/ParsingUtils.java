package me.colormaestro.taskmanager.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

class ParsingUtils {

    /**
     * Sets integer or null value at given index for prepared statement
     *
     * @param statement in which to set value
     * @param index numbered from 1
     * @param value to set
     */
    static void setIntOrNull(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value != null) {
            statement.setInt(index, value);
        } else {
            statement.setNull(index, Types.INTEGER);
        }
    }

    /**
     * Gets integer value or null for given column
     *
     * @param resultSet describing returned records from query
     * @param columnName column from which to extract value
     * @return Integer instance
     * @throws SQLException if SQL error arise
     */
    static Integer getIntOrNull(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        if (resultSet.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    /**
     * Gets integer value or null for given column
     *
     * @param resultSet describing returned records from query
     * @param columnName column from which to extract value
     * @return Integer instance
     * @throws SQLException if SQL error arise
     */
    static Long getLongOrNull(ResultSet resultSet, String columnName) throws SQLException {
        long value = resultSet.getInt(columnName);
        if (resultSet.wasNull()) {
            return null;
        } else {
            return value;
        }
    }
}
