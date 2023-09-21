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
import io.frankmayer.papermcwebapi.utils.HtmlTemplate;
import io.frankmayer.papermcwebapi.utils.JWT;
import io.frankmayer.papermcwebapi.utils.NamespacedKeys;
import io.frankmayer.papermcwebapi.utils.JWT.Response;

public class AuthorizeHandler extends HttpHandlerWrapper {
    private static final HtmlTemplate LOGIN_TEMPLATE = new HtmlTemplate()
            .p("%s").p("%s").ul("%s")
            .p("We have sent you a code in the Minecraft chat, enter it here and confirm the login.")
            .form("get", "%s", false,
                    new HtmlTemplate()
                            .input("text", "code", true)
                            .input("submit", null, false, "Login")
                            .input("hidden", "login", true, "%s")
                            .input("hidden", "client_id", true, "%s"));

    public String getRoute() {
        return "authorize";
    }

    public Object get(final HttpExchange t, final OfflinePlayer authorized) {
        final Map<String, List<String>> query = HttpFrontend.parseQueryParameters(t.getRequestURI().getQuery());
        final String clientId = HttpFrontend.firstOrThrow(query, "client_id");
        final String login = HttpFrontend.firstOrThrow(query, "login");
        final Optional<String> codeIn = HttpFrontend.firstOrNone(query, "code");

        // is this a phase 2 request?
        if (codeIn.isPresent()) {
            return phaseTwoRequest(t, clientId, login, codeIn.get());
        }

        // this is a phase 1 request
        return phaseOneRequest(t, clientId, login);
    }

    private String phaseOneRequest(final HttpExchange t, final String clientId, final String login) {
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
                return LOGIN_TEMPLATE.process(
                        String.format("Login as <b>%s<b>?", HttpFrontend.escapeHtml(bukkitPlayer.getName())),
                        HttpFrontend.escapeHtml(
                                String.format("%s gets access to the following permissions:", c.getName())),
                        HtmlTemplate.li(c.getScopes()),
                        HttpFrontend.LISTENING + "authorize",
                        HttpFrontend.escapeHtmlParam(login),
                        HttpFrontend.escapeHtmlParam(clientId));
            }
        }
        throw new IllegalArgumentException("invalid client_id");
    }

    private Response phaseTwoRequest(
            final HttpExchange t,
            final String clientId,
            final String login,
            final String codeIn) {
        final Player bukkitPlayer = io.frankmayer.papermcwebapi.backend.Player.getBukkitPlayer(login);
        if (bukkitPlayer == null) {
            throw new IllegalArgumentException("invalid login, not an online player");
        }
        final String code = bukkitPlayer.getPersistentDataContainer().get(NamespacedKeys.code(),
                PersistentDataType.STRING);
        bukkitPlayer.getPersistentDataContainer().remove(NamespacedKeys.code());
        if (!codeIn.equals(code)) {
            throw new IllegalArgumentException("invalid code");
        }
        final JWT.Response response = new JWT.Response(clientId, bukkitPlayer.getUniqueId().toString());
        bukkitPlayer.sendMessage("§2You have been logged in successfully.");
        t.getResponseHeaders().add("Location", Main.PREFERENCES.getClientById(clientId).get().getRedirectUri());
        HttpFrontend.sendJWT(t, response);
        return response;
    }
}
