package io.frankmayer.papermcwebapi;

import java.io.OutputStream;
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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.frankmayer.papermcwebapi.exceptions.UnauthorizedException;
import io.frankmayer.papermcwebapi.utils.Cached;
import io.frankmayer.papermcwebapi.utils.JWT;
import io.frankmayer.papermcwebapi.utils.NamespacedKeys;
import io.frankmayer.papermcwebapi.utils.Posix;
import io.frankmayer.papermcwebapi.utils.Str;

public class HttpFrontend {
    private static abstract class HttpHandlerWrapper implements HttpHandler {

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

    private static class HelloWorldHandler extends HttpHandlerWrapper {
        public String get(final HttpExchange t, final OfflinePlayer authorized) {
            t.getResponseHeaders().add("Cache-Control", "max-age=10");
            return "Hello World!";
        }
    }

    private static class OnlinePlayersHandler extends HttpHandlerWrapper {
        private final Cached<String> cached = new Cached<>(5000);

        public String get(final HttpExchange t, final OfflinePlayer authorized) {
            t.getResponseHeaders().add("Cache-Control", "max-age=5");
            t.getResponseHeaders().add("Content-Type", "application/json");
            return this.cached.get(io.frankmayer.papermcwebapi.backend.Player::getOnlinePlayers);
        }
    }

    private static class AuthorizeHandler extends HttpHandlerWrapper {
        public String get(final HttpExchange t, final OfflinePlayer authorized) {
            final Map<String, List<String>> query = HttpFrontend.parseQueryParameters(t.getRequestURI().getQuery());
            final String clientId = HttpFrontend.firstOrThrow(query.get("client_id"), "client_id");
            final String login = HttpFrontend.firstOrThrow(query.get("login"), "login");
            final Optional<String> codeIn = HttpFrontend.firstOrNone(query.get("code"));

            // is this a phase 2 request?
            if (codeIn.isPresent()) {
                final Player bukkitPlayer = io.frankmayer.papermcwebapi.backend.Player.getBukkitPlayer(login);
                if (bukkitPlayer == null) {
                    throw new IllegalArgumentException("invalid login, not an online player");
                }
                final String code = bukkitPlayer.getPersistentDataContainer().get(NamespacedKeys.code(),
                        PersistentDataType.STRING);
                bukkitPlayer.getPersistentDataContainer().remove(NamespacedKeys.code());
                if (!codeIn.get().equals(code)) {
                    throw new IllegalArgumentException("invalid code");
                }
                final JWT.Response response = new JWT.Response(clientId, bukkitPlayer.getUniqueId().toString());
                t.getResponseHeaders().add("Content-Type", "application/json");
                bukkitPlayer.sendMessage("§2You have been logged in successfully.");
                t.getResponseHeaders().add("Location", Main.PREFERENCES.getClientById(clientId).get().getRedirectUri());
                HttpFrontend.sendJWT(t, response);
                return Main.GSON.toJson(response);
            }

            // this is a phase 1 request
            for (final var c : Main.PREFERENCES.getClients()) {
                if (c.getId().equals(clientId)) {
                    final Player bukkitPlayer = io.frankmayer.papermcwebapi.backend.Player.getBukkitPlayer(login);
                    if (bukkitPlayer == null) {
                        throw new IllegalArgumentException("invalid login, not an online player");
                    }

                    final String code = HttpFrontend.hash(login + Double.toString(Math.random()));
                    bukkitPlayer.sendMessage(String.format(
                            "Someone is trying to login as you on §9%s§r,\nYour code is §9%s§r.\nIf this was not you, please ignore this message.\nIf this was you, please enter the code on the website.",
                            c.getName(), code));
                    bukkitPlayer.getPersistentDataContainer().set(NamespacedKeys.code(), PersistentDataType.STRING,
                            code);

                    t.getResponseHeaders().add("Content-Type", "text/html");
                    final StringBuilder perm = new StringBuilder();
                    for (final var p : c.getPermissions()) {
                        perm.append(String.format("<li>%s</li>", HttpFrontend.escapeHtml(p)));
                    }
                    return String.format(
                            "<p>%s</p><p>%s</p><ul>%s</ul><p>We have sent you a code in the Minecraft chat, enter it here and confirm the login.</p><form action=\"%s\" method=\"get\"><input type=\"text\" name=\"code\" required/><input type=\"submit\" value=\"Login\"/><input type=\"hidden\" name=\"login\" value=\"%s\"/><input type=\"hidden\" name=\"client_id\" value=\"%s\"/></form>",
                            String.format("Login as <b>%s<b>?", HttpFrontend.escapeHtml(bukkitPlayer.getName())),
                            HttpFrontend.escapeHtml(
                                    String.format("%s gets access to the following permissions:", c.getName())),
                            perm.toString(),
                            HttpFrontend.LISTENING + "authorize",
                            login.replaceAll("\"", "&quot;"),
                            clientId.replaceAll("\"", "&quot;"));
                }
            }
            throw new IllegalArgumentException("invalid client_id");
        }
    }

    private static class ProfilePictureHandler extends HttpHandlerWrapper {
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
                    authorized.getName().replaceAll("\"", "&quot;"));
        }
    }

    private static class CommandHandler extends HttpHandlerWrapper {
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

    private static MessageDigest md;

    private static @NotNull String LISTENING;

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

    public static String hash(final String string) {
        return Base64.getUrlEncoder().encodeToString(HttpFrontend.getMd().digest(string.getBytes())).substring(0, 4);
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
            HttpFrontend.LISTENING = String.format("http://localhost:%d%s", port, Posix.join("/", basePath, "/"));
            Main.LOGGER.info("Listening on " + HttpFrontend.LISTENING);
            this.server.createContext(Posix.join("/", basePath, "/hello_world"), new HelloWorldHandler());
            this.server.createContext(Posix.join("/", basePath, "/online_players"), new OnlinePlayersHandler());
            this.server.createContext(Posix.join("/", basePath, "/authorize"), new AuthorizeHandler());
            this.server.createContext(Posix.join("/", basePath, "/profile_picture"), new ProfilePictureHandler());
            this.server.createContext(Posix.join("/", basePath, "/command"), new CommandHandler());
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
