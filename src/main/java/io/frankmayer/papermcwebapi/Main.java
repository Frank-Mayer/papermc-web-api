package io.frankmayer.papermcwebapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;

import io.frankmayer.papermcwebapi.lua.Lua;
import io.frankmayer.papermcwebapi.lua.LuaCommand;

public final class Main extends JavaPlugin {
    public static @NotNull Main INSTANCE;
    public static @NotNull Logger LOGGER;
    public static @NotNull Server SERVER;
    public static @NotNull Preferences PREFERENCES;
    public static @NotNull final Gson GSON = new Gson();

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
        Main.PREFERENCES = Preferences.load(this.getDataFolder());
        try {
            Lua.init();
            if (Main.PREFERENCES.getLuaCmd()) {
                Main.SERVER.getCommandMap().register("lua", new LuaCommand());
            }
            this.httpFrontend = new HttpFrontend(Main.PREFERENCES.getBasePath(), Main.PREFERENCES.getHttpPort());
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
