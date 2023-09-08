package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class Lua {
    private static final Globals globals = Lua.makeGlobals();

    public static LuaValue exec(final String script) throws LuaError {
        return globals.load(script).call();
    }

    private static Globals makeGlobals() {
        final Globals globals = JsePlatform.standardGlobals();
        globals.set(GetPlayerData.NAME, new GetPlayerData());
        return globals;
    }

    private Lua() {
    }
}
