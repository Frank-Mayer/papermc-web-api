package io.frankmayer.papermcwebapi.lua;

import org.bukkit.persistence.PersistentDataType;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;

import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.utils.NamespacedKeys;

class GetPlayerData extends ThreeArgFunction {
    public static final String NAME = "getPlayerData";

    @Override
    public LuaValue call(final LuaValue player, final LuaValue key, final LuaValue type) {
        try {
            final var bukkitPlayer = Main.SERVER.getPlayer(player.checkjstring());
            if (bukkitPlayer == null) {
                return LuaValue.NIL;
            }

            switch (type.checkjstring().toUpperCase()) {
                case "STRING": {
                    final String value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.STRING);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "INTEGER": {
                    final Integer value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.INTEGER);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "LONG": {
                    final Long value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.LONG);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "DOUBLE": {
                    final Double value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.DOUBLE);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "FLOAT": {
                    final Float value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.FLOAT);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "BYTE_ARRAY": {
                    final byte[] value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.BYTE_ARRAY);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "INTEGER_ARRAY": {
                    final int[] value = bukkitPlayer.getPersistentDataContainer()
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
                    final long[] value = bukkitPlayer.getPersistentDataContainer()
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
                    final Byte value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.BYTE);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "SHORT": {
                    final Short value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.SHORT);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                case "BOOLEAN": {
                    final Boolean value = bukkitPlayer.getPersistentDataContainer()
                            .get(NamespacedKeys.custom(key.checkjstring()), PersistentDataType.BOOLEAN);
                    if (value == null) {
                        return LuaValue.NIL;
                    }
                    return LuaValue.valueOf(value);
                }
                default: {
                    throw new IllegalArgumentException(String.format(
                            "Invalid type: %s for lua function getPlayerData",
                            type.checkjstring()));
                }
            }
        } catch (final Exception e) {
            return LuaValue.NIL;
        }
    }
}
