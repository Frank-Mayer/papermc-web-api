package io.frankmayer.papermcwebapi.backend;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import io.frankmayer.papermcwebapi.Main;

public class Player {
    public static String getOnlinePlayers() {
        final Player[] players = Main.SERVER.getOnlinePlayers().stream()
                .map(Player::new)
                .toArray(Player[]::new);
        return Main.GSON.toJson(players);
    }

    public static org.bukkit.entity.Player getBukkitPlayer(final String player) {
        try {
            final UUID uuid = UUID.fromString(player);
            final org.bukkit.entity.Player bukkitPlayer = Main.SERVER.getPlayer(uuid);
            if (bukkitPlayer != null) {
                return bukkitPlayer;
            }
        } catch (final Exception e) {
        }

        try {
            final org.bukkit.entity.Player bukkitPlayer = Main.SERVER.getPlayer(player);
            if (bukkitPlayer != null) {
                return bukkitPlayer;
            }
        } catch (final Exception e) {
        }

        return null;
    }

    public static OfflinePlayer getBukkitOfflinePlayer(final String player) {
        final var onlinePlayer = Player.getBukkitPlayer(player);
        if (onlinePlayer != null) return onlinePlayer;

        try {
            final UUID uuid = UUID.fromString(player);
            final OfflinePlayer bukkitPlayer = Main.SERVER.getOfflinePlayer(uuid);
            if (bukkitPlayer != null) {
                return bukkitPlayer;
            }
        } catch (final Exception e) {
        }

        try {
            final OfflinePlayer bukkitPlayer = Main.SERVER.getOfflinePlayer(player);
            if (bukkitPlayer != null) {
                return bukkitPlayer;
            }
        } catch (final Exception e) {
        }

        return null;
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

    protected void setName(final String name) {
        this.name = name;
    }

    protected void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
