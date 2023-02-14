package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SolarFurnaceResultSet {

    private final SolarFurnaceDatabase service = SolarFurnaceDatabase.getInstance();
    private final Connection connection = service.getConnection();
    private final BromleyMassiveTools plugin;

    private int furnaceId;
    private Location detectorLocation;
    private Location furnaceLocation;
    private final Set<Location> detectorSet = new HashSet<>();
    private final Set<Location> furnaceSet = new HashSet<>();

    public SolarFurnaceResultSet(BromleyMassiveTools plugin) {
        this.plugin = plugin;
    }

    public boolean fromLocation(String location, String field) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        final String query = "SELECT * FROM furnaces WHERE " + field + " = ?";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, location);
            rs = statement.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                furnaceId = rs.getInt("furnace_id");
                detectorLocation = getLocationFromBukkitString(rs.getString("detector_location"));
                furnaceLocation = getLocationFromBukkitString(rs.getString("furnace_location"));
                return true;
            }
            return false;
        } catch (SQLException e) {
            plugin.debug("ResultSet error for furnaces table (fromLocation)! " + e.getMessage());
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                plugin.debug("Error closing furnaces table (fromLocation)! " + e.getMessage());
            }
        }
    }

    public void getLocations() {
        Statement statement = null;
        ResultSet rs = null;
        final String query = "SELECT * FROM furnaces";
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
            if (rs.isBeforeFirst()) {
                rs.next();
                detectorSet.add(getLocationFromBukkitString(rs.getString("detector_location")));
                furnaceSet.add(getLocationFromBukkitString(rs.getString("furnace_location")));
            }
        } catch (SQLException e) {
            plugin.debug("ResultSet error for furnaces table (getLocations)! " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                plugin.debug("Error closing furnaces table (getLocations)! " + e.getMessage());
            }
        }
    }

    public int getFurnaceId() {
        return furnaceId;
    }

    public Location getDetectorLocation() {
        return detectorLocation;
    }

    public Location getFurnaceLocation() {
        return furnaceLocation;
    }

    public Set<Location> getDetectorSet() {
        return detectorSet;
    }

    public Set<Location> getFurnaceSet() {
        return furnaceSet;
    }

    /**
     * Gets a location object from data stored in the database.
     *
     * @param string the stored Bukkit location string e.g. Location{world=CraftWorld{name=world},x=0.0,y=0.0,z=0.0,pitch=0.0,yaw=0.0}
     * @return the location or null
     */
    private Location getLocationFromBukkitString(String string) {
        //Location{world=CraftWorld{name=world},x=0.0,y=0.0,z=0.0,pitch=0.0,yaw=0.0}
        String[] loc_data = string.split(",");
        // w, x, y, z - 0, 1, 2, 3
        String[] wStr = loc_data[0].split("=");
        String[] xStr = loc_data[1].split("=");
        String[] yStr = loc_data[2].split("=");
        String[] zStr = loc_data[3].split("=");
        String tmp = wStr[2].substring(0, (wStr[2].length() - 1));
        World w = Bukkit.getServer().getWorld(tmp);
        if (w == null) {
            return null;
        }
        // Location{world=CraftWorld{name=world},x=1.0000021E7,y=67.0,z=1824.0,pitch=0.0,yaw=0.0}
        double x = parseDouble(xStr[1]);
        double y = parseDouble(yStr[1]);
        double z = parseDouble(zStr[1]);
        return new Location(w, x, y, z);
    }

    /**
     * Parses a string for a double.
     *
     * @param d the string to convert to an double.
     * @return a floating point number
     */
    private double parseDouble(String d) {
        double num = 0.0d;
        try {
            num = Double.parseDouble(d);
        } catch (NumberFormatException n) {
            plugin.debug("Could not convert to double, the string was: " + d);
        }
        return num;
    }
}
