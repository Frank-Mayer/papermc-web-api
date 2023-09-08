package io.frankmayer.papermcwebapi.lua;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LuaCommand extends Command {
    public LuaCommand() {
        super("lua");
        this.setDescription("Execute a lua script");
        this.setUsage("/lua <lua code>");
        this.setPermission("io.frankmayer.papermcwebapi.lua");
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

        try {
            sender.sendMessage(Lua.exec(String.join(" ", args)).toString());
        } catch (final Exception e) {
            sender.sendMessage(e.getMessage());
        }
        return true;
    }
}
