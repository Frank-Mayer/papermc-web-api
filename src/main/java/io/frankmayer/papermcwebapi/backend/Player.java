package io.frankmayer.papermcwebapi.backend;

import io.frankmayer.papermcwebapi.Main;

public class Player {
    public static String getOnlinePlayers() {
        final Player[] players = Main.SERVER.getOnlinePlayers().stream()
                .map(Player::new)
                .toArray(Player[]::new);
        return Main.GSON.toJson(players);
    }

    private String name;

    private String uuid;

    public Player(final org.bukkit.entity.Player player) {
        this.name = player.getName();
        this.uuid = player.getUniqueId().toString();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
