package io.frankmayer.papermcwebapi.handler;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.exceptions.UnauthorizedException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class AchievementsHandler extends HttpHandlerWrapper {
    private static class Achievement {
        @SuppressWarnings("unused")
        public final String id;

        @SuppressWarnings("unused")
        public final String name;

        @SuppressWarnings("unused")
        public final Component nameFormatted;

        @SuppressWarnings("unused")
        public final boolean done;

        public Achievement(final String id, final String name, final Component nameFormatted, final boolean done) {
            this.id = id;
            this.name = name;
            this.nameFormatted = nameFormatted;
            this.done = done;
        }
    }

    final @NotNull PlainTextComponentSerializer textSer = PlainTextComponentSerializer.plainText();

    public String getRoute() {
        return "achievements";
    }

    public ArrayList<AchievementsHandler.Achievement> get(final HttpExchange t, final OfflinePlayer authorized) {
        if (authorized == null) {
            throw new UnauthorizedException("not authorized");
        }

        if (authorized instanceof final Player player) {
            final var ach = new ArrayList<AchievementsHandler.Achievement>();
            final @NotNull Iterator<Advancement> iter = Bukkit.advancementIterator();
            while (iter.hasNext()) {
                final var achievement = iter.next();
                ach.add(new AchievementsHandler.Achievement(
                        achievement.getKey().toString(),
                        textSer.serialize(achievement.displayName()),
                        achievement.displayName(),
                        player.getAdvancementProgress(achievement).isDone()));
            }
            return ach;
        }

        throw new IllegalArgumentException("Player needs to be online in order to get achievements");
    }
}
