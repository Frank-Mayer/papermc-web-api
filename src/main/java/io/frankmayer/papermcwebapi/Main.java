package io.frankmayer.papermcwebapi;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private HttpFrontend httpFrontend;

    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            this.httpFrontend = new HttpFrontend(8080);
        } catch (Exception e) {
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
