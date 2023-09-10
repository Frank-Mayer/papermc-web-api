package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import io.frankmayer.papermcwebapi.backend.Player;

public class SendMessage extends TwoArgFunction {

	@Override
	public LuaValue call(final LuaValue receiver, final LuaValue message) {
        if (receiver.istable()) {
            for (final LuaValue r : new TableIterable(receiver.checktable())) {
                this.call(r, message);
            }
            return LuaValue.NIL;
        }

        if (receiver.isfunction()) {
            return this.call(receiver.checkfunction().call(), message);
        }

        if (message.istable()) {
            for (final LuaValue m : new TableIterable(message.checktable())) {
                this.call(receiver, m);
            }
            return LuaValue.NIL;
        }

        if (message.isfunction()) {
            return this.call(receiver, message.checkfunction().call());
        }

        Player.sendMessage(receiver.checkjstring(), message.checkjstring());

        return LuaValue.NIL;
	}
}
