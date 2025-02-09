package org.example.leedshack.busPosVizPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class BusPosVizPlugin extends JavaPlugin {
    public static BusPosVizPlugin instance;



    @Override
    public void onEnable() {
        instance = this;
        CommandHandler.registerCommands();
    }

    @Override
    public void onDisable() {
    }
}
