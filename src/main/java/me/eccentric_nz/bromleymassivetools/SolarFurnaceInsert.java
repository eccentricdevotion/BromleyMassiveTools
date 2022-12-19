package me.eccentric_nz.bromleymassivetools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SolarFurnaceInsert {

    private final BromleyMassiveTools plugin;
    private final SolarFurnaceDatabase service = SolarFurnaceDatabase.getInstance();
    private final Connection connection = service.getConnection();

    /**
     * Inserts data into an SQLite database table. This method builds a prepared SQL statement from the parameters
     * supplied and then executes the insert.
     *
     * @param plugin an instance of the main plugin class
     */
    public SolarFurnaceInsert(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    public void addFurnace(String detector, String furnace) {
        PreparedStatement ps = null;
        ResultSet idRS = null;
        try {
            ps = connection.prepareStatement("INSERT INTO furnaces (detector_location, furnace_location) VALUES (?, ?)");
            ps.setString(1, detector);
            ps.setString(2, furnace);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.debug("Insert error for furnaces! " + e.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                plugin.debug("Error closing furnaces! " + e.getMessage());
            }
        }
    }
}
