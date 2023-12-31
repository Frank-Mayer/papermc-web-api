package io.frankmayer.papermcwebapi;

import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.frankmayer.papermcwebapi.handler.HttpHandlerWrapper;
import io.frankmayer.papermcwebapi.utils.JWT;
import io.frankmayer.papermcwebapi.utils.Path;
import io.frankmayer.papermcwebapi.utils.Str;

public class HttpFrontend {
    private static MessageDigest md;
    public static @NotNull String LISTENING;

    public static void sendJWT(final HttpExchange t, final JWT.Response resp) {
        final var headers = t.getResponseHeaders();
        headers.add("Set-Cookie", String.format("refresh_token=%s;Expires=%s;Path=%s;HttpOnly",
                resp.refreshToken,
                resp.refreshExpires,
                Main.PREFERENCES.getBasePath()));
        headers.add("Set-Cookie", String.format("access_token=%s;Expires=%s;Path=%s;HttpOnly",
                resp.accessToken,
                resp.accessExpires,
                Main.PREFERENCES.getBasePath()));
    }

    public static String escapeHtml(final String format) {
        return format.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static String escapeHtmlParam(final String format) {
        return format.replace("\"", "&quot;").replace("&", "&amp;");
    }

    public static String hash(final String string) {
        return Base64.getUrlEncoder().encodeToString(HttpFrontend.getMd().digest(string.getBytes())).substring(0, 4);
    }

    public static <T> T firstOrThrow(final Map<String, List<T>> query, final String key) {
        return HttpFrontend.firstOrThrow(query.get(key), key);
    }

    public static <T> Optional<T> firstOrNone(final Map<String, List<T>> query, final String key) {
        return HttpFrontend.firstOrNone(query.get(key));
    }

    public static Map<String, List<String>> parseQueryParameters(final String query) {
        final Map<String, List<String>> queryParams = new LinkedHashMap<>();

        if (query != null) {
            final List<String> pairs = Str.split(query, '&');
            for (final String pair : pairs) {
                final List<String> keyValue = Str.split(pair, '=');
                if (keyValue.size() == 2) {
                    try {
                        final String key = URLDecoder.decode(keyValue.get(0), "UTF-8");
                        final String value = URLDecoder.decode(keyValue.get(1), "UTF-8");
                        queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                    } catch (final UnsupportedEncodingException e) {
                        throw new IllegalArgumentException("Failed to decode query parameter", e);
                    }
                }
            }
        }

        return queryParams;
    }

    private static <T> Optional<T> firstOrNone(final List<T> list) {
        if (list == null || list.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    private static <T> T firstOrThrow(final List<T> list, final String name) {
        if (list == null || list.size() != 1) {
            throw new IllegalArgumentException("please provide exactly one " + name);
        }
        return list.get(0);
    }

    private static MessageDigest getMd() {
        if (HttpFrontend.md != null) {
            return HttpFrontend.md;
        }

        try {
            HttpFrontend.md = MessageDigest.getInstance("MD2");
            HttpFrontend.md.update("papermc-web-api".getBytes());
            return HttpFrontend.md;
        } catch (final Exception e) {
            Main.panic("Failed to get MessageDigest", e);
            return null;
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
            final Reflections reflections = new Reflections("io.frankmayer.papermcwebapi.handler");
            HttpFrontend.LISTENING = String.format("http://localhost:%d%s", port, Path.joinPosix("/", basePath, "/"));
            Main.LOGGER.info("Listening on " + HttpFrontend.LISTENING);
            for (final var handlerClass : reflections.getSubTypesOf(HttpHandlerWrapper.class)) {
                final var handler = handlerClass.getDeclaredConstructor().newInstance();
                final String route = Path.joinPosix("/", basePath, "/", handler.getRoute());
                Main.LOGGER.info(String.format("Registering HTTP handler %s", route));
                this.server.createContext(route, handler);
            }
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
