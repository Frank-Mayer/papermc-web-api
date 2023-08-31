package io.frankmayer.papermcwebapi.handler;

import org.bukkit.OfflinePlayer;

import com.sun.net.httpserver.HttpExchange;

public class HelloWorldHandler extends HttpHandlerWrapper {
    public String get(final HttpExchange t, final OfflinePlayer authorized) {
        t.getResponseHeaders().add("Cache-Control", "max-age=10");
        return "Hello World!";
    }
}
