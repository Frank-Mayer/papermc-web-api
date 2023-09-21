package io.frankmayer.papermcwebapi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.frankmayer.papermcwebapi.gson.GsonEnhancement;
import io.frankmayer.papermcwebapi.lua.Lua;
import io.frankmayer.papermcwebapi.lua.LuaCommand;

public final class Main extends JavaPlugin {
    public static @NotNull Main INSTANCE;
    public static @NotNull Logger LOGGER;
    public static @NotNull Server SERVER;
    public static @NotNull Preferences PREFERENCES;
    public static @NotNull Gson GSON;

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

    public Main() {
        Main.INSTANCE = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Main.LOGGER = this.getLogger();
        Main.SERVER = this.getServer();

        final var g = new GsonBuilder();
        final var refl = new Reflections("io.frankmayer.papermcwebapi.gson");
        final var adapters = refl.getSubTypesOf(GsonEnhancement.class);
        for (final var taClass : adapters) {
			try {
				final var ta = taClass.getDeclaredConstructor().newInstance();
                Main.LOGGER.log(Level.INFO, "Registering GsonEnhancement: " + ta.forType.getName());
                g.registerTypeAdapter(ta.forType, ta);
			} catch (final Exception e) {
				Main.panic(e);
			}
        }
        Main.GSON = g.create();

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
