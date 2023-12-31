package io.frankmayer.papermcwebapi.handler;

import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.HttpFrontend;
import io.frankmayer.papermcwebapi.lua.Lua;
import io.frankmayer.papermcwebapi.utils.Permissions;

public class LuaHandler extends HttpHandlerWrapper {
    private static class Response {
        @SuppressWarnings("unused")
        public final String returnValue;

        public Response(final LuaValue returnValue) {
            this.returnValue = returnValue.toString();
        }
    }

    private static boolean customCodeAllowed(final OfflinePlayer authorized) {
        if (authorized == null)
            return false;

        if (authorized.isOp())
            return true;

        if (authorized instanceof final Player onlinePlayer)
            return onlinePlayer.hasPermission(Permissions.LUA);

        return false;
    }

    public String getRoute() {
        return "lua";
    }

    public LuaHandler.Response get(final HttpExchange t, final OfflinePlayer authorized) {
        final Map<String, List<String>> query = HttpFrontend.parseQueryParameters(t.getRequestURI().getQuery());
        final String script = HttpFrontend.firstOrThrow(query, "script");

        if (Lua.scriptExists(script)) {
            return new LuaHandler.Response(Lua.runScript(script, authorized));
        }

        if (LuaHandler.customCodeAllowed(authorized)) {
            return new LuaHandler.Response(Lua.exec(script));
        }

        throw new IllegalArgumentException(String.format("script %s not found", script));
    }
}
