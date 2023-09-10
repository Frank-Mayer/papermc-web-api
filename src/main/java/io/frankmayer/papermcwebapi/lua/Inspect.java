package io.frankmayer.papermcwebapi.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;

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

    private static String inspect(final LuaValue arg) {
        return Inspect.inspect(arg, 1);
    }

    private static String inspect(final LuaValue arg, final int depth) {
        if (arg.isnil()) {
            return Inspect.NIL;
        } else if (arg.isboolean()) {
            return arg.toboolean() ? Inspect.TRUE : Inspect.FALSE;
        } else if (arg.isnumber()) {
            return arg.tojstring();
        } else if (arg.isstring()) {
            return Inspect.smartQuote(arg.tojstring());
        } else if (arg.isfunction()) {
            return String.format("function %s", arg.checkfunction().name());
        } else if (arg.isthread()) {
            return Inspect.THREAD;
        } else if (arg.istable()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            LuaValue key = LuaValue.NIL;
            while (true) {
                final Varargs n = arg.next(key);
                if ((key = n.arg1()).isnil())
                    break;
                final LuaValue value = n.arg(2);
                Inspect.appendIndentDepth(sb, depth);
                sb.append('[');
                sb.append(Inspect.smartQuote(key.tojstring()));
                sb.append(']');
                sb.append(" = ");
                final String inspectedValue = Inspect.inspect(value, depth + 1);
                sb.append(inspectedValue);
                sb.append(",\n");
            }
            Inspect.appendIndentDepth(sb, depth - 1);
            sb.append('}');
            return sb.toString();
        } else {
            return Inspect.UNKNOWN;
        }
    }

    private static String smartQuote(final String str) {
        final StringBuilder sb = new StringBuilder(str.length() + 2);
        sb.append('"');
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    @Override
    public LuaString call(final LuaValue arg) {
        return LuaString.valueOf(Inspect.inspect(arg));
    }

}
