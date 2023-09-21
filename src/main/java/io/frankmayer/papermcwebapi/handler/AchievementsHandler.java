package io.frankmayer.papermcwebapi.handler;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.exceptions.UnauthorizedException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class AchievementsHandler extends HttpHandlerWrapper {
    private static record Achievement(String id, String name, Component nameFormatted, boolean done) {
    }

    final @NotNull PlainTextComponentSerializer textSer = PlainTextComponentSerializer.plainText();

    public String getRoute() {
        return "achievements";
    }

    public String get(final HttpExchange t, final OfflinePlayer authorized) {
        if (authorized == null) {
            throw new UnauthorizedException("not authorized");
        }

        if (authorized instanceof final Player player) {
            final var ach = new ArrayList<AchievementsHandler.Achievement>();
            final var iter = Bukkit.advancementIterator();
            while (iter.hasNext()) {
                final var achievement = iter.next();
                if (player.getAdvancementProgress(achievement).isDone()) {
                    ach.add(new Achievement(
                            achievement.getKey().toString(),
                            textSer.serialize(achievement.displayName()),
                            achievement.displayName(),
                            player.getAdvancementProgress(achievement).isDone()));
                }
            }
            t.getResponseHeaders().add("Content-Type", "application/json");
            return Main.GSON.toJson(ach);
        }

        throw new IllegalArgumentException("Player needs to be online in order to get achievements");
    }
}
