package io.frankmayer.papermcwebapi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.frankmayer.papermcwebapi.backend.Player;
import io.frankmayer.papermcwebapi.utils.Cached;
import io.frankmayer.papermcwebapi.utils.JWT;
import io.frankmayer.papermcwebapi.utils.Posix;

class HttpFrontend {
    private static abstract class HttpHandlerWrapper implements HttpHandler {
        private String response = "";
        private OutputStream os = null;
        private int statusCode = 200;

        @Override
        public void handle(final HttpExchange t) {
            try {
                this.response = this.get(t);
                this.statusCode = 200;
            } catch (final IllegalArgumentException e) {
                this.response = e.getMessage();
                this.statusCode = 400;
            } catch (final Exception e) {
                this.response = e.getMessage();
                this.statusCode = 500;
            } finally {
                try {
                    t.sendResponseHeaders(this.statusCode, this.response.length());
                    this.os = t.getResponseBody();
                    this.os.write(this.response.getBytes());
                    this.os.close();
                } catch (final Exception e) {
                    Main.panic("Failed to write response body", e);
                }
            }
        }

        protected abstract String get(final HttpExchange t) throws Exception;
    }

    private static class HelloWorldHandler extends HttpHandlerWrapper {
        public String get(final HttpExchange t) {
            t.getResponseHeaders().add("Cache-Control", "max-age=10");
            return "Hello World!";
        }
    }

    private static class OnlinePlayersHandler extends HttpHandlerWrapper {
        private final Cached<String> cached = new Cached<>(5000);

        public String get(final HttpExchange t) {
            t.getResponseHeaders().add("Cache-Control", "max-age=5");
            t.getResponseHeaders().add("Content-Type", "application/json");
            return this.cached.get(io.frankmayer.papermcwebapi.backend.Player::getOnlinePlayers);
        }
    }

    private static class AuthorizeHandler extends HttpHandlerWrapper {
        public String get(final HttpExchange t) {
            final Map<String, List<String>> query = HttpFrontend.parseQueryParameters(t.getRequestURI().getQuery());
            final String clientId = HttpFrontend.firstOrThrow(query.get("client_id"), "client_id");
            final Optional<String> redirectUri = HttpFrontend.firstOrNone(query.get("redirect_uri"));
            final Optional<String> login = HttpFrontend.firstOrNone(query.get("login"));
            // t.getResponseHeaders().add("Content-Type", "text/html");
            t.getResponseHeaders().add("Content-Type", "text/plain");
            final String[] x = new String[] { "client_id", clientId, "redirect_uri", redirectUri.orElse("null"), "login", login.orElse("null") };
            final JWT<String[]> jwt = new JWT<>(x);
            return jwt.toString();
        }
    }

    private static <T> T firstOrThrow(final List<T> list, final String name) {
        if (list == null || list.size() != 1) {
            throw new IllegalArgumentException("please provide exactly one " + name);
        }
        return list.get(0);
    }

    private static <T> Optional<T> firstOrNone(final List<T> list) {
        if (list == null || list.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    private static Map<String, List<String>> parseQueryParameters(final String query) {
        final Map<String, List<String>> queryParams = new LinkedHashMap<>();

        if (query != null) {
            final String[] pairs = query.split("&");
            for (final String pair : pairs) {
                final String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        final String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        final String value = URLDecoder.decode(keyValue[1], "UTF-8");
                        queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException("Failed to decode query parameter", e);
                    }
                }
            }
        }

        return queryParams;
    }

    private final HttpServer server;

    public HttpFrontend(final String basePath, final int port) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (final BindException e) {
            Main.panic(String.format("Failed to bind to port %d, is it already in use?", port), e);
            throw new RuntimeException(e);
        } catch (final Exception e) {
            Main.panic("Failed to create HTTP server", e);
            throw new RuntimeException(e);
        }
        try {
            Main.LOGGER.info(String.format("Listening on http://localhost:%d%s", port, Posix.join("/", basePath, "/")));
            this.server.createContext(Posix.join("/", basePath, "/hello_world"), new HelloWorldHandler());
            this.server.createContext(Posix.join("/", basePath, "/online_players"), new OnlinePlayersHandler());
            this.server.createContext(Posix.join("/", basePath, "/authorize"), new AuthorizeHandler());
            this.server.setExecutor(null);
            this.server.start();
        } catch (final Exception e) {
            Main.panic("Failed to start HTTP server", e);
        }
    }

    public void dispose() {
        this.server.stop(0);
    }
}
