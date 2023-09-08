package io.frankmayer.papermcwebapi.utils;

public class Path {
    public static String joinPosix(final String... strings) {
        final StringBuilder sb = new StringBuilder();
        boolean slash = false;
        for (final String string : strings) {
            for (final char c : string.toCharArray()) {
                if (c == '/') {
                    if (!slash) {
                        sb.append(c);
                        slash = true;
                    }
                } else {
                    sb.append(c);
                    slash = false;
                }
            }
        }
        return sb.toString();
    }

    private Path() {
    }
}
