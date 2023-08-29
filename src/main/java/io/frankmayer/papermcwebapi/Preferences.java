package io.frankmayer.papermcwebapi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class Preferences {
    public static Preferences load(final File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        final File file = new File(folder, "preferences.jsonc");
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            if (file.exists()) {
                // read using gson
                try (final FileReader reader = new FileReader(file)) {
                    final Preferences preferences = gson.fromJson(reader, Preferences.class);
                    return preferences;
                }
            }
        } catch (final Exception e) {
            Main.panic("Failed to load preferences", e);
            return null;
        }

        // write using gson
        final Preferences preferences = new Preferences();
        final String json = gson.toJson(preferences);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            try (final FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
        } catch (final Exception e) {
            Main.panic("Failed to save preferences", e);
            return null;
        }
        return preferences;
    }

    private int httpPort = 8080;

    public int getHttpPort() {
        return httpPort;
    }

    protected void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
    }
}
