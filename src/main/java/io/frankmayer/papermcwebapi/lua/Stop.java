package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import io.frankmayer.papermcwebapi.Main;

public class Stop extends ZeroArgFunction {
	@Override
	public LuaValue call() {
        Main.SERVER.shutdown();
        return LuaValue.NIL;
	}
}
