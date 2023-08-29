package io.frankmayer.papermcwebapi;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private HttpFrontend httpFrontend;

    @Override
    public void onEnable() {
        // Plugin startup logic
        final Preferences preferences = Preferences.load(this.getDataFolder());
        this.getLogger().info("httpPort: " + preferences.getHttpPort());
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
        this.httpFrontend.dispose();
    }
}
