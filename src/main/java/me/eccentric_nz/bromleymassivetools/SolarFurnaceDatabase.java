package me.eccentric_nz.bromleymassivetools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SolarFurnaceDatabase {

    private static final SolarFurnaceDatabase instance = new SolarFurnaceDatabase();
    public Connection connection = null;
    public Statement statement = null;

    public static synchronized SolarFurnaceDatabase getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public void createTables() {
        try {
            statement = connection.createStatement();
            // Table structure for table 'items'
            final String queryFurnaces = "CREATE TABLE IF NOT EXISTS furnaces (furnace_id INTEGER PRIMARY KEY NOT NULL, detector_location TEXT DEFAULT '', furnace_location TEXT DEFAULT '')";
            statement.executeUpdate(queryFurnaces);
        } catch (SQLException e) {
            System.err.println("[SolarFurnace] Error creating tables: " + e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                System.err.println("[SolarFurnace] Error closing SQL statement: " + e.getMessage());
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
}
