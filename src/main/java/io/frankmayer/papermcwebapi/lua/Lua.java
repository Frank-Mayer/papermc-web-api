package io.frankmayer.papermcwebapi.lua;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.reflections.Reflections;

import io.frankmayer.papermcwebapi.Main;

public class Lua {
    private static @NotNull Globals globals;
    protected static final Map<String, LuaValue> ApiFunctionRegister = new HashMap<>();

    public static LuaValue exec(final String script) throws LuaError {
        return Lua.globals.load(script).call();
    }

    public static boolean scriptExists(final String script) {
        return Lua.ApiFunctionRegister.containsKey(script);
    }

    public static LuaValue runScript(final String script, final OfflinePlayer authorized) {
        final LuaValue authUUID = authorized == null ? LuaValue.NIL
                : LuaValue.valueOf(authorized.getUniqueId().toString());
        return Lua.ApiFunctionRegister.get(script).call(authUUID);
    }

    public static Globals init() {
        final Globals globals = JsePlatform.standardGlobals();

        // Register the Paper API
        final LuaTable paper = new LuaTable();
        final Reflections reflections = new Reflections("io.frankmayer.papermcwebapi.lua");
        for (final var luaValueClass : reflections.getSubTypesOf(LuaValue.class)) {
            try {
                final String name = Lua.pascalToCamelCase(luaValueClass.getSimpleName());
                final LuaValue instance = luaValueClass.getDeclaredConstructor().newInstance();
                Main.LOGGER.info(String.format("Registering Lua API function %s", name));
                paper.set(name, instance);
            } catch (final Exception ignored) {
            }
        }
        globals.set("Paper", paper);

        // register ./plugins/PaperMCWebAPI/lua/ as a lua path
        final File dir = Paths.get(Main.INSTANCE.getDataFolder().getAbsolutePath(), "lua").toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // set package.path to include the lua directory
        final LuaValue packageTable = globals.get("package");
        packageTable.set("path", LuaString.valueOf(Paths.get(dir.getAbsolutePath(), "?.lua").toString()));

        // Load the init.lua file
        final File init = Paths.get(dir.getAbsolutePath(), "init.lua").toFile();
        if (init.exists()) {
            try {
                globals.loadfile(init.getAbsolutePath()).call();
            } catch (final Exception e) {
                Main.panic(e);
            }
        }
        return globals;
    }

    private static String pascalToCamelCase(final String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    private Lua() {
    }
}
