package io.frankmayer.papermcwebapi.lua;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import io.frankmayer.papermcwebapi.utils.Permissions;

public class LuaCommand extends Command {
    public LuaCommand() {
        super("lua");
        this.setDescription("Execute a lua script");
        this.setUsage("/lua <lua code>");
        this.setPermission(Permissions.LUA);
    }

    @Override
    public boolean execute(
            @NotNull final CommandSender sender,
            @NotNull final String commandLabel,
            @NotNull final String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (sender != null && !sender.isOp() && !sender.hasPermission(this.getPermission())) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        final String script = String.join(" ", args);

        try {
            if (Lua.scriptExists(script)) {
                final Player authorized = sender instanceof Player ? (Player) sender : null;
                sender.sendMessage(Lua.runScript(script, authorized).toString());
                return true;
            }
            sender.sendMessage(Lua.exec(script).toString());
        } catch (final Exception e) {
            sender.sendMessage("§c" + e.getMessage());
        }
        return true;
    }
}
