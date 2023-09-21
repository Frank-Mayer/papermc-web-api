package io.frankmayer.papermcwebapi.handler;

import java.io.OutputStream;

import org.bukkit.OfflinePlayer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.exceptions.UnauthorizedException;
import io.frankmayer.papermcwebapi.utils.JWT;

public abstract class HttpHandlerWrapper implements HttpHandler {
    public static void setHeaderIfNotExists(final Headers respHeaders, final String key, final String value) {
        if (!respHeaders.containsKey(key)) {
            respHeaders.add(key, value);
        }
    }

    private static byte[] getBytesAndSetHeader(final Headers respHeaders, final Object o) {
        if (o instanceof String) {
            HttpHandlerWrapper.setHeaderIfNotExists(respHeaders, "Content-Type", "text/plain");
            return ((String) o).getBytes();
        } else if (o instanceof byte[]) {
            return (byte[]) o;
        } else if (o == null) {
            return new byte[0];
        } else if (o instanceof Number) {
            HttpHandlerWrapper.setHeaderIfNotExists(respHeaders, "Content-Type", "text/plain");
            return ((Integer) o).toString().getBytes();
        } else if (o instanceof Boolean) {
            final var bArr = new byte[1];
            bArr[0] = ((Boolean) o) ? (byte) 1 : (byte) 0;
            return bArr;
        } else if (o instanceof Iterable) {
            HttpHandlerWrapper.setHeaderIfNotExists(respHeaders, "Content-Type", "application/json");
            try {
                return Main.GSON.toJson(o).getBytes();
            } catch (final Exception e) {
                Main.panic("Failed to convert object to json", e);
                return new byte[0];
            }
        } else {
            try {
                HttpHandlerWrapper.setHeaderIfNotExists(respHeaders, "Content-Type", "application/json");
                return Main.GSON.toJson(o).getBytes();
            } catch (final Exception e) {
                Main.panic("Failed to convert object to json", e);
                return new byte[0];
            }
        }
    }

    @Override
    public void handle(final HttpExchange t) {
        Object response = null;
        OutputStream os = null;
        int statusCode = 200;
        final OfflinePlayer authorized = JWT.processAuth(t);
        final Headers respHeaders = t.getResponseHeaders();
        try {
            response = this.get(t, authorized);
            if (respHeaders.containsKey("Location")) {
                statusCode = 302;
            } else {
                statusCode = 200;
            }
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
                final byte[] respByteArr = HttpHandlerWrapper.getBytesAndSetHeader(respHeaders, response);
                t.sendResponseHeaders(statusCode, respByteArr.length);
                os = t.getResponseBody();
                os.write(respByteArr);
                os.close();
            } catch (final Exception e) {
                Main.panic("Failed to write response body", e);
            }
        }
    }

    public abstract String getRoute();

    protected abstract Object get(final HttpExchange t, final OfflinePlayer authorized) throws Exception;
}
