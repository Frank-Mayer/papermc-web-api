package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class RegisterApiFunction extends TwoArgFunction {
    @Override
    public LuaValue call(final LuaValue name, final LuaValue function) {
        if (name.isstring() && function.isfunction())
            Lua.ApiFunctionRegister.put(name.checkjstring(), function);

        return LuaValue.NIL;
    }
}
