
package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import io.frankmayer.papermcwebapi.backend.Player;

public class SendBroadcastMessage extends OneArgFunction {

    @Override
    public LuaValue call(final LuaValue message) {
        if (message.istable()) {
            for (final LuaValue m : new TableIterable(message.checktable())) {
                Player.broadcastMessage(m.checkjstring());
            }
            return LuaValue.NIL;
        } else if (message.isfunction()) {
            Player.broadcastMessage(message.checkfunction().call().checkjstring());
        }

        Player.broadcastMessage(message.checkjstring());

        return LuaValue.NIL;
    }
}
