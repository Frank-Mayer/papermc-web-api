package io.frankmayer.papermcwebapi.utils;

import java.util.ArrayList;
import java.util.List;

import io.frankmayer.papermcwebapi.Main;

public class Str {
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

    public static String html(final String body) {
        return String.format(
                "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>%s</title><style>:root {color-scheme: light dark;}</style></head><body>%s</body></html>",
                Main.INSTANCE.getName(),
                body);
    }

    public static String exclude(final String text, final char c) {
        final StringBuilder sb = new StringBuilder();
        for (final char ch : text.toCharArray()) {
            if (ch != c) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String replace(final String text, final char c, final char replacement) {
        final StringBuilder sb = new StringBuilder(text.length());
        for (final char ch : text.toCharArray()) {
            if (ch == c) {
                sb.append(replacement);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String replace(final String text, final String c, final String replacement) {
        final StringBuilder sb = new StringBuilder(text.length());
        for (final char ch : text.toCharArray()) {
            if (c.indexOf(ch) != -1) {
                sb.append(replacement);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private Str() {
    }

    public static String quote(String string) {
        return '"' +
                Str.replace(Str.replace(string, "\\", "\\\\"), "\"", "\\\"") +
                '"';
    }
}
