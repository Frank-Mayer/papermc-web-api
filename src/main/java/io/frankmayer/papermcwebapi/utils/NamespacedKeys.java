package io.frankmayer.papermcwebapi.utils;

import org.bukkit.NamespacedKey;

import io.frankmayer.papermcwebapi.Main;

public class NamespacedKeys {
    private static NamespacedKey CODE;

    public static NamespacedKey code() {
        if (NamespacedKeys.CODE == null) {
            NamespacedKeys.CODE = new NamespacedKey(Main.INSTANCE, "code");
        }
        return NamespacedKeys.CODE;
    }

    private NamespacedKeys() {
    }
}
