package io.frankmayer.papermcwebapi.handler;

import java.io.File;
import java.nio.file.Files;

import org.bukkit.OfflinePlayer;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.Main;

public class StyleHandler extends HttpHandlerWrapper {
    public String getRoute() {
        return "style.css";
    }

    private static String styleSheetCache = null;

    public String get(final HttpExchange t, final OfflinePlayer authorized) throws Exception {
        t.getResponseHeaders().add("Cache-Control", "max-age=86400, public"); // 1 day
        t.getResponseHeaders().add("Content-Type", "text/css");
        if (StyleHandler.styleSheetCache == null) {
            final var styleSheetFile = new File(Main.INSTANCE.getDataFolder(), "style.css");
            final var styleSheetPath = styleSheetFile.toPath();
            if (styleSheetFile.exists()) {
                StyleHandler.styleSheetCache = Files.readString(styleSheetPath);
            } else {
                StyleHandler.styleSheetCache = ":root { color-scheme: light dark; }";
                Files.writeString(styleSheetPath, StyleHandler.styleSheetCache);
            }
        }
        return StyleHandler.styleSheetCache;
    }
}
