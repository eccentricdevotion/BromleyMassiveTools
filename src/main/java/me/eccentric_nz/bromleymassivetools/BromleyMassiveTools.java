package me.eccentric_nz.bromleymassivetools;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Set;

public class BromleyMassiveTools extends JavaPlugin {

    private final OreCounter oreCounter = new OreCounter(this);
    private final OreBroadcast oreBroadcast = new OreBroadcast(this);
    private final SolarFurnaceDatabase service = SolarFurnaceDatabase.getInstance();
    public String pluginName;
    private Set<Location> furnaces;
    private Set<Location> detectors;

    @Override
    public void onDisable() {
        try {
            if (service.connection != null) {
                service.connection.close();
            }
        } catch (SQLException e) {
            debug("Could not close database connection: " + e);
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdfFile = getDescription();
        pluginName = ChatColor.GOLD + "[" + pdfFile.getName() + "]" + ChatColor.RESET + " ";
        // setup database
        try {
            String path = getDataFolder() + File.separator + "SolarFurnace.db";
            service.setConnection(path);
            service.createTables();
        } catch (Exception e) {
            getServer().getConsoleSender().sendMessage(pluginName + "Connection and Tables Error: " + e);
        }
        // load furnaces from database
        loadFurnaces();
        // register listeners
        pm.registerEvents(new ExplosionHealerListener(this), this);
        pm.registerEvents(new OreAnnouncerListener(this), this);
        pm.registerEvents(new SolarFurnaceListener(this), this);
        pm.registerEvents(new DeathListener(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new SolarFurnaceRunnable(this), 100L, 202L);
        // load commands
        BromleyMassiveToolsCommand command = new BromleyMassiveToolsCommand(this);
        for (String cmd : pdfFile.getCommands().keySet()) {
            getCommand(cmd).setExecutor(command);
        }
    }

    public String getPluginName() {
        return pluginName;
    }

    public OreCounter getOreCounter() {
        return oreCounter;
    }

    public OreBroadcast getOreBroadcast() {
        return oreBroadcast;
    }

    private void loadFurnaces() {
        SolarFurnaceResultSet furnaceResultSet = new SolarFurnaceResultSet(this);
        furnaceResultSet.getLocations();
        detectors = furnaceResultSet.getDetectorSet();
        furnaces = furnaceResultSet.getFurnaceSet();
    }

    public Set<Location> getFurnaces() {
        return furnaces;
    }

    public Set<Location> getDetectors() {
        return detectors;
    }

    public void debug(Object o) {
        getServer().getConsoleSender().sendMessage(pluginName + o);
    }
}