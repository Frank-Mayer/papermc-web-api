package io.frankmayer.papermcwebapi.lua;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import io.frankmayer.papermcwebapi.Main;

class GetEntityName extends OneArgFunction {
    @Override
    public LuaValue call(final LuaValue entity) {
        if (entity.isnil()) {
            return LuaValue.NIL;
        }

        if (entity.istable()) {
            final var names = new LuaTable();
            int i = 1;
            for (final LuaValue e : new TableIterable(entity.checktable())) {
                names.set(i++, this.call(e));
            }
            return names;
        }

        if (entity.isfunction()) {
            return this.call(entity.checkfunction().call());
        }

        try {
            final UUID uuid = UUID.fromString(entity.checkjstring());
            final Entity bukkitEntity = Main.SERVER.getEntity(uuid);
            if (bukkitEntity != null) {
                return LuaValue.valueOf(bukkitEntity.getName());
            }
            final OfflinePlayer player = Main.SERVER.getOfflinePlayer(uuid);
            if (player != null && player.hasPlayedBefore()) {
                return LuaValue.valueOf(player.getName());
            }
            return LuaValue.NIL;
        } catch (final Exception e) {
            return LuaValue.NIL;
        }
    }
}
