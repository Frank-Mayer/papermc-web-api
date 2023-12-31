package io.frankmayer.papermcwebapi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Base64;
import java.util.Optional;

import com.google.gson.GsonBuilder;

import io.frankmayer.papermcwebapi.utils.JWT;
import io.frankmayer.papermcwebapi.utils.Path;

public class Preferences {
    public static class Client {
        private String name;
        private String id;
        private String redirectUri;
        private String[] scopes = new String[0];

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(final String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String[] getScopes() {
            return scopes;
        }

        public void setScopes(final String[] scopes) {
            this.scopes = scopes;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }
    }

    public static Preferences load(final File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        final File file = new File(folder, "preferences.jsonc");

        try {
            if (file.exists()) {
                // read using gson
                try (final FileReader reader = new FileReader(file)) {
                    final Preferences preferences = Main.GSON.fromJson(reader, Preferences.class);
                    return preferences;
                }
            }
        } catch (final Exception e) {
            Main.panic("Failed to load preferences", e);
            return null;
        }

        // write using gson
        final Preferences preferences = new Preferences();
        final String json = new GsonBuilder().setPrettyPrinting().create().toJson(preferences);
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
    private String basePath = "";
    private String secret = Base64.getUrlEncoder().encodeToString(
            JWT.getMd().digest(
                    Double.toString(Math.random()).getBytes()))
            +
            Base64.getUrlEncoder().encodeToString(
                    JWT.getMd().digest(
                            Double.toString(Math.random()).getBytes()));
    private Client[] clients = new Client[0];
    private boolean luaCmd = false;

    public boolean getLuaCmd() {
        return this.luaCmd;
    }

    public void setLuaCmd(final boolean enabled) {
        this.luaCmd = enabled;
    }

    public Client[] getClients() {
        return clients;
    }

    public void setClients(final Client[] clients) {
        this.clients = clients;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getBasePath() {
        return Path.joinPosix("/", basePath);
    }

    public String getSecret() {
        return secret;
    }

    public void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
    }

    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public Optional<Client> getClientById(final String clientId) {
        for (final Client client : this.clients) {
            if (client.getId().equals(clientId)) {
                return Optional.of(client);
            }
        }
        return Optional.empty();
    }
}
