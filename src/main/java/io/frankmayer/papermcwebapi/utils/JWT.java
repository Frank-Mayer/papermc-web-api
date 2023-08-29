package io.frankmayer.papermcwebapi.utils;

import java.security.MessageDigest;
import java.util.Base64;

import io.frankmayer.papermcwebapi.Main;

public class JWT<Payload> {
    public static class Header {
        private final String alg = "HS512";
        private final String typ = "JWT";

        public String getAlg() {
            return alg;
        }

        public String getTyp() {
            return typ;
        }
    }

    private static MessageDigest md;

    private static MessageDigest getMd() {
        if (JWT.md != null) {
            return JWT.md;
        }

        try {
            JWT.md = MessageDigest.getInstance("SHA-512");
            JWT.md.update("papermc-web-api".getBytes());
            return JWT.md;
        } catch (final Exception e) {
            Main.panic("Failed to get MessageDigest", e);
            return null;
        }
    }

    private static String base64UrlEncode(final String input) {
        return Base64.getUrlEncoder().encodeToString(input.getBytes());
    }

    private static String hash(final String input) {
        return JWT.getMd().digest(input.getBytes()).toString();
    }

    public final Header header;

    public final Payload payload;

    public final String token;

    public JWT(final Payload payload) {
        this.header = new Header();
        this.payload = payload;
        this.token = String.format("%s.%s.%s",
                (Main.GSON.toJson(this.header)),
                (Main.GSON.toJson(this.payload)),
                (Main.PREFERENCES.getSecret()));
    }

    @Override
    public String toString() {
        return this.token;
    }
}
