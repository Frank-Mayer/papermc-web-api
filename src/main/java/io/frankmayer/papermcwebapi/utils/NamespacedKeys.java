package io.frankmayer.papermcwebapi.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;

import io.frankmayer.papermcwebapi.Main;

public class NamespacedKeys {
    private static NamespacedKey CODE;
    private static final Map<String, NamespacedKey> CUSTOM = new HashMap<>();

    public static NamespacedKey code() {
        if (NamespacedKeys.CODE == null) {
            NamespacedKeys.CODE = new NamespacedKey(Main.INSTANCE, "code");
        }
        return NamespacedKeys.CODE;
    }

    public static NamespacedKey custom(String key) {
        if (NamespacedKeys.CUSTOM.containsKey(key)) {
            return NamespacedKeys.CUSTOM.get(key);
        }
        var namespacedKey = new NamespacedKey(Main.INSTANCE, key);
        NamespacedKeys.CUSTOM.put(key, namespacedKey);
        return namespacedKey;
    }

    private NamespacedKeys() {
    }
}
