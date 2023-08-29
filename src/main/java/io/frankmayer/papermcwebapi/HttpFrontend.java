package io.frankmayer.papermcwebapi;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.frankmayer.papermcwebapi.utils.Cached;

class HttpFrontend {
    private static abstract class HttpHandlerWrapper implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) {
            String response;
            try {
                response = this.get(t);
                t.sendResponseHeaders(200, response.length());
            } catch (final Exception e) {
                response = e.getMessage();
                try {
                    t.sendResponseHeaders(500, response.length());
                } catch (final IOException e1) {
                    Main.panic("Failed to send response headers", e1);
                }
            }
            final OutputStream os = t.getResponseBody();
            try {
                os.write(response.getBytes());
                os.close();
            } catch (final IOException e) {
                Main.panic("Failed to write response", e);
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
