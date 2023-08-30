package io.frankmayer.papermcwebapi.handler;

import java.io.OutputStream;

import org.bukkit.OfflinePlayer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.exceptions.UnauthorizedException;
import io.frankmayer.papermcwebapi.utils.JWT;

abstract class HttpHandlerWrapper implements HttpHandler {

    @Override
    public void handle(final HttpExchange t) {
        String response = "";
        OutputStream os = null;
        int statusCode = 200;
        final OfflinePlayer authorized = JWT.processAuth(t);
        try {
            response = this.get(t, authorized);
            statusCode = 200;
        } catch (final IllegalArgumentException e) {
            response = e.getMessage();
            statusCode = 400;
        } catch (final UnauthorizedException e) {
            response = e.getMessage();
            statusCode = 401;
        } catch (final Exception e) {
            response = e.getMessage();
            statusCode = 500;
        } finally {
            try {
                if (t.getResponseHeaders().containsKey("Location")) {
                    statusCode = 302;
                }
                t.sendResponseHeaders(statusCode, response.length());
                os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (final Exception e) {
                Main.panic("Failed to write response body", e);
            }
        }
    }

    protected abstract String get(final HttpExchange t, final OfflinePlayer authorized) throws Exception;
}
