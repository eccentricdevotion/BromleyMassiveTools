package me.eccentric_nz.bromleymassivetools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

public class SolarFurnaceDelete {

    private final SolarFurnaceDatabase service = SolarFurnaceDatabase.getInstance();
    private final Connection connection = service.getConnection();
    private final BromleyMassiveTools plugin;

    public SolarFurnaceDelete(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    /**
     * Deletes rows from an SQLite database table. This method builds an SQL query string from the parameters supplied
     * and then executes the delete.
     *
     * @param table the database table name to insert the data into.
     * @param where a HashMap<String, Object> of table fields and values to select the records to delete.
     */
    public void removeRecord(String table, HashMap<String, Object> where) {
        Statement statement = null;
        String values;
        StringBuilder sbw = new StringBuilder();
        where.forEach((key, value) -> {
            sbw.append(key).append(" = ");
            if (value instanceof String || value instanceof UUID) {
                sbw.append("'").append(value.toString()).append("' AND ");
            } else {
                sbw.append(value).append(" AND ");
            }
        });
        where.clear();
        values = sbw.toString().substring(0, sbw.length() - 5);
        String query = "DELETE FROM furnaces WHERE " + values;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            plugin.debug("Delete error for " + table + "! " + e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                plugin.debug("Error closing " + table + "! " + e.getMessage());
            }
        }
    }

    public int removeByLocation(String location, String field) {
        PreparedStatement ps = null;
        final String query = "DELETE FROM furnaces WHERE " + field + " = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, location);
            return ps.executeUpdate();
        } catch (SQLException e) {
            plugin.debug("Delete error for furnaces table! " + e.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                plugin.debug("Error closing furnaces table! " + e.getMessage());
            }
        }
        return 0;
    }
}
