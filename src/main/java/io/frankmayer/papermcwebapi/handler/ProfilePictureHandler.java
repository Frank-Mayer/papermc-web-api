package io.frankmayer.papermcwebapi.handler;

import org.bukkit.OfflinePlayer;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.HttpFrontend;
import io.frankmayer.papermcwebapi.exceptions.UnauthorizedException;

public class ProfilePictureHandler extends HttpHandlerWrapper {
    public String get(final HttpExchange t, final OfflinePlayer authorized) {
        if (authorized == null) {
            throw new UnauthorizedException("not authorized");
        }
        final var respHeaders = t.getResponseHeaders();
        respHeaders.add("Cache-Control", "max-age=3600");
        final String url = "https://mc-heads.net/head/" + authorized.getName();
        respHeaders.add("Location", url);
        return String.format(
                "<img src=\"%s\" alt=\"%s\" loading=\"lazy\" decoding=\"async\"/>",
                url,
                HttpFrontend.escapeHtmlParam(authorized.getName()));
    }
}
