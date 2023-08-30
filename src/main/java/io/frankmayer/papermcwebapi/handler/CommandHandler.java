package io.frankmayer.papermcwebapi.handler;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.HttpFrontend;
import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.exceptions.UnauthorizedException;

public class CommandHandler extends HttpHandlerWrapper {
    public String get(final HttpExchange t, final OfflinePlayer authorized) {
        if (authorized == null) {
            throw new UnauthorizedException("not authorized");
        }
        final Player onlinePlayer = authorized.getPlayer();
        if (onlinePlayer == null) {
            throw new IllegalArgumentException("player is not online");
        }
        final Map<String, List<String>> query = HttpFrontend.parseQueryParameters(t.getRequestURI().getQuery());
        final String command = HttpFrontend.firstOrThrow(query.get("command"), "command");
        Bukkit.getScheduler().runTask(Main.INSTANCE, () -> onlinePlayer.performCommand(command));
        return "OK";
    }
}
