package io.frankmayer.papermcwebapi.lua;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import io.frankmayer.papermcwebapi.Main;

class GetEntityName extends OneArgFunction {
    @Override
    public LuaValue call(final LuaValue entity) {
        try {
            final var uuid = UUID.fromString(entity.checkjstring());
            final Entity bukkitEntity = Main.SERVER.getEntity(uuid);
            return LuaValue.valueOf(bukkitEntity.getName());
        } catch (final Exception e) {
            return LuaValue.NIL;
        }
    }
}
