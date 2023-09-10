package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class RegisterApiFunction extends TwoArgFunction {
    @Override
    public LuaValue call(final LuaValue name, final LuaValue function) {
        if (!name.isstring()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid name: %s for lua function registerApiFunction",
                    Inspect.inspect(name)));
        }

        if (!function.isfunction()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid function: %s for lua function registerApiFunction",
                    Inspect.inspect(function)));
        }

        Lua.ApiFunctionRegister.put(name.checkjstring(), function);
        return LuaValue.NIL;
    }
}
