package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;

import io.frankmayer.papermcwebapi.utils.Str;

/**
 * Gets a human-readable string representation of the given object.
 */
public class Inspect extends OneArgFunction {

    private static final String NIL = "nil";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String INDENT = "        ";
    private static final String UNKNOWN = "<unknown>";
    private static final String THREAD = "<thread>";

    private static void appendIndentDepth(final StringBuilder sb, final int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append(Inspect.INDENT);
        }
    }

    public static String inspect(final LuaValue arg) {
        return Inspect.inspect(arg, 1, false);
    }

    public static String inspect(final LuaValue arg, final boolean newLines) {
        return Inspect.inspect(arg, 1, newLines);
    }

    private static String inspect(final LuaValue arg, final int depth, final boolean newLines) {
        if (arg.isnil()) {
            return Inspect.NIL;
        } else if (arg.isboolean()) {
            return arg.toboolean() ? Inspect.TRUE : Inspect.FALSE;
        } else if (arg.isnumber()) {
            return arg.tojstring();
        } else if (arg.isstring()) {
            return Str.smartQuote(arg.tojstring());
        } else if (arg.isfunction()) {
            return String.format("function %s", arg.checkfunction().name());
        } else if (arg.isthread()) {
            return Inspect.THREAD;
        } else if (arg.istable()) {
            final StringBuilder sb = new StringBuilder();
            if (newLines) {
                sb.append("{\n");
            } else {
                sb.append("{");
            }
            LuaValue key = LuaValue.NIL;
            while (true) {
                final Varargs n = arg.next(key);
                if ((key = n.arg1()).isnil())
                    break;
                final LuaValue value = n.arg(2);
                Inspect.appendIndentDepth(sb, depth);
                sb.append('[');
                sb.append(Str.smartQuote(key.tojstring()));
                sb.append(']');
                sb.append(" = ");
                final String inspectedValue = Inspect.inspect(value, depth + 1, newLines);
                sb.append(inspectedValue);
                if (newLines) {
                    sb.append(",\n");
                } else {
                    sb.append(", ");
                }
            }
            Inspect.appendIndentDepth(sb, depth - 1);
            sb.append('}');
            return sb.toString();
        } else {
            return Inspect.UNKNOWN;
        }
    }

    @Override
    public LuaString call(final LuaValue arg) {
        return LuaString.valueOf(Inspect.inspect(arg, true));
    }

}
