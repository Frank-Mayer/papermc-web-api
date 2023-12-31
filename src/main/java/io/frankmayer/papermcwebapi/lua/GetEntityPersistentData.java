package io.frankmayer.papermcwebapi.lua;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;

import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.utils.NamespacedKeys;

class GetEntityPersistentData extends ThreeArgFunction {
    @Override
    public LuaValue call(final LuaValue entity, final LuaValue key, final LuaValue type) {
        if (entity.isnil() || key.isnil() || type.isnil()) {
            return LuaValue.NIL;
        }

        if (key.istable() || key.isfunction()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid key: %s for lua function getPlayerData",
                    Inspect.inspect(key)));
        }

        if (type.istable() || type.isfunction()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid type: %s for lua function getPlayerData",
                    Inspect.inspect(type)));
        }

        if (entity.istable()) {
            final var names = new LuaTable();
            int i = 1;
            for (final LuaValue e : new TableIterable(entity.checktable())) {
                names.set(i++, this.call(e, key, type));
            }
            return names;
        }

        if (entity.isfunction()) {
            return this.call(entity.checkfunction().call(), key, type);
        }

        try {
            final var uuid = UUID.fromString(entity.checkjstring());
            final Entity bukkitEntity = Main.SERVER.getEntity(uuid);
            if (bukkitEntity == null) {
                return LuaValue.NIL;
            }

            switch (type.checkjstring().toUpperCase()) {
                case "STRING": {
                    final String value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.STRING);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "INTEGER": {
                    final Integer value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.INTEGER);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "LONG": {
                    final Long value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.LONG);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "DOUBLE": {
                    final Double value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.DOUBLE);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "FLOAT": {
                    final Float value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.FLOAT);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "BYTE_ARRAY": {
                    final byte[] value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.BYTE_ARRAY);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "INTEGER_ARRAY": {
                    final int[] value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.INTEGER_ARRAY);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    final var table = new LuaTable();
                    for (int i = 0; i < value.length; i++) {
                        table.set(i + 1, LuaValue.valueOf(value[i]));
                    }
                    return table;
                }
                case "LONG_ARRAY": {
                    final long[] value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.LONG_ARRAY);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    final var table = new LuaTable();
                    for (int i = 0; i < value.length; i++) {
                        table.set(i + 1, LuaValue.valueOf(value[i]));
                    }
                    return table;
                }
                case "BYTE": {
                    final Byte value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.BYTE);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "SHORT": {
                    final Short value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.SHORT);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "BOOLEAN": {
                    final Boolean value = bukkitEntity.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.BOOLEAN);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                default: {
                    throw new IllegalArgumentException(String.format(
                            "Invalid type: %s for lua function getPlayerData",
                            Inspect.inspect(type)));
                }
            }
        } catch (final Exception e) {
            return LuaValue.NIL;
        }
    }
}
