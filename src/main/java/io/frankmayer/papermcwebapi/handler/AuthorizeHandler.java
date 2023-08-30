package io.frankmayer.papermcwebapi.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.HttpFrontend;
import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.utils.JWT;
import io.frankmayer.papermcwebapi.utils.NamespacedKeys;

public class AuthorizeHandler extends HttpHandlerWrapper {
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
                        "<p>%s</p><p>%s</p><ul>%s</ul><p>We have sent you a code in the Minecraft chat, enter it here and confirm the login.</p><form autocomplete=\"off\" action=\"%s\" method=\"get\"><input type=\"text\" name=\"code\" required/><input type=\"submit\" value=\"Login\"/><input type=\"hidden\" name=\"login\" value=\"%s\"/><input type=\"hidden\" name=\"client_id\" value=\"%s\"/></form>",
                        String.format("Login as <b>%s<b>?", HttpFrontend.escapeHtml(bukkitPlayer.getName())),
                        HttpFrontend.escapeHtml(
                                String.format("%s gets access to the following permissions:", c.getName())),
                        perm.toString(),
                        HttpFrontend.LISTENING + "authorize",
                        HttpFrontend.escapeHtmlParam(login),
                        HttpFrontend.escapeHtmlParam(clientId));
            }
        }
        throw new IllegalArgumentException("invalid client_id");
    }
}
