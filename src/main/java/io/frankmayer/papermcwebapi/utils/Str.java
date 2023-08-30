package io.frankmayer.papermcwebapi.utils;

import java.util.ArrayList;
import java.util.List;

public class Str {
    private Str() {
    }

    public static List<String> split(final String string, final char delimiter) {
        final StringBuilder sb = new StringBuilder();
        final List<String> list = new ArrayList<>();

        for (final char c : string.toCharArray()) {
            if (c == delimiter) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());

        return list;
    }
}
