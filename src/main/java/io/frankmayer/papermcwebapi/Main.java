package io.frankmayer.papermcwebapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin {
    public static @NotNull Main INSTANCE;
    public static @NotNull Logger LOGGER;

    private HttpFrontend httpFrontend;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Main.INSTANCE = this;
        Main.LOGGER = this.getLogger();
        final Preferences preferences = Preferences.load(this.getDataFolder());
        try {
            this.httpFrontend = new HttpFrontend(preferences.getHttpPort());
        } catch (final Exception e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (this.httpFrontend != null) {
            this.httpFrontend.dispose();
        }
    }

    public static void panic(String string) {
        Main.LOGGER.log(Level.WARNING, string);
        Main.INSTANCE.getServer().getPluginManager().disablePlugin(Main.INSTANCE);
    }

    public static void panic(String string, Exception e) {
        Main.LOGGER.log(Level.WARNING, string, e);
        Main.INSTANCE.getServer().getPluginManager().disablePlugin(Main.INSTANCE);
    }

    public static void panic(Exception e) {
        Main.LOGGER.log(Level.WARNING, e.getMessage(), e);
        Main.INSTANCE.getServer().getPluginManager().disablePlugin(Main.INSTANCE);
    }
}
