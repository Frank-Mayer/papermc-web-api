package io.frankmayer.papermcwebapi;

class Posix {
    public static String join(final String... strings) {
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

    private Posix() {
    }
}
