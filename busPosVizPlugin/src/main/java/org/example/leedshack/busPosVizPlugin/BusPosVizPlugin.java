package org.example.leedshack.busPosVizPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Sheep;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.example.leedshack.busPosVizPlugin.listeners.CustomPlayerMoveEvent;

public final class BusPosVizPlugin extends JavaPlugin {
    public static BusPosVizPlugin instance;
    public static final String STOPS_FILENAME = "D:\\Programming\\Hackathons\\leedshack2025\\busPosVizPlugin\\Stops.csv";
    public static final double RADIUS = 200;

    public static final Map<String, Sheep> SHEEPS = new HashMap<>();
    public static final Map<String, GlobalLocation> SHEEP_PREV_LOCATIONS = new HashMap<>();

    public static List<BusStop> BusStops = new ArrayList<>();
    public static List<BusStopMC> BusStopsMC = new ArrayList<>();;

    public static BukkitTask SHEEP_TASK;

    @Override
    public void onEnable() {
        instance = this;
        CommandHandler.registerCommands();

        World world = this.getServer().getWorlds().getFirst();
        world.getBlockAt(new Location(world, 0, -61, 0)).setType(Material.BLACK_CONCRETE);

        getServer().getPluginManager().registerEvents(new CustomPlayerMoveEvent(), this);
    }

    @Override
    public void onDisable() {
    }
}
