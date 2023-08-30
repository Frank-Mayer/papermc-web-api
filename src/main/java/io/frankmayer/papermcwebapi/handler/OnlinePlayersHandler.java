package io.frankmayer.papermcwebapi.handler;

import org.bukkit.OfflinePlayer;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.utils.Cached;

public class OnlinePlayersHandler extends HttpHandlerWrapper {
    private final Cached<String> cached = new Cached<>(5000);

    public String get(final HttpExchange t, final OfflinePlayer authorized) {
        t.getResponseHeaders().add("Cache-Control", "max-age=5");
        t.getResponseHeaders().add("Content-Type", "application/json");
        return this.cached.get(io.frankmayer.papermcwebapi.backend.Player::getOnlinePlayers);
    }
}
