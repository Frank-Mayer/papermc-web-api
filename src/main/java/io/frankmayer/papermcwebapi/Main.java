package io.frankmayer.papermcwebapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Main extends JavaPlugin {
    public static @NotNull Main INSTANCE;
    public static @NotNull Logger LOGGER;
    public static @NotNull Server SERVER;
    public static @NotNull final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void panic(final String string) {
        Main.LOGGER.log(Level.WARNING, string);
        Main.INSTANCE.getServer().getPluginManager().disablePlugin(Main.INSTANCE);
    }

    public static void panic(final String string, final Exception e) {
        Main.LOGGER.log(Level.WARNING, string, e);
        Main.INSTANCE.getServer().getPluginManager().disablePlugin(Main.INSTANCE);
    }

    public static void panic(final Exception e) {
        Main.LOGGER.log(Level.WARNING, e.getMessage(), e);
        Main.INSTANCE.getServer().getPluginManager().disablePlugin(Main.INSTANCE);
    }

    private HttpFrontend httpFrontend;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Main.INSTANCE = this;
        Main.LOGGER = this.getLogger();
        Main.SERVER = this.getServer();
        final Preferences preferences = Preferences.load(this.getDataFolder());
        try {
            this.httpFrontend = new HttpFrontend(preferences.getBasePath(), preferences.getHttpPort());
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
}
