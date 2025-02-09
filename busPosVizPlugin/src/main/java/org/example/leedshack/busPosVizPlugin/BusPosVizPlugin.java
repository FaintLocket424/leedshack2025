package org.example.leedshack.busPosVizPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class BusPosVizPlugin extends JavaPlugin {
    public static BusPosVizPlugin instance;
    public static final String STOPS_FILENAME = "D:\\Programming\\Hackathons\\leedshack2025\\busPosVizPlugin\\Stops.csv";

    @Override
    public void onEnable() {
        instance = this;
        CommandHandler.registerCommands();
    }

    @Override
    public void onDisable() {
    }
}
