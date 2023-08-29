package io.frankmayer.papermcwebapi;

class Posix {
    public static String join(final String... strings) {
        final StringBuilder sb = new StringBuilder();
        for (final String string : strings) {
            sb.append(string);
        }
        return sb.toString().replace("\\", "/").replace("//", "/");
    }

    private Posix() {
    }
}
