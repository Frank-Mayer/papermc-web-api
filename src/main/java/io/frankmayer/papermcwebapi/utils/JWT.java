package io.frankmayer.papermcwebapi.utils;

import java.security.MessageDigest;
import java.util.Base64;

import io.frankmayer.papermcwebapi.Main;

public class JWT {
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

    public static class Payload {
        private String tokenType;
        private String clientId;
        private String uuid;
        private long iat;

        public Payload(final String tokenType, final String clientId, final String uuid, final long duration) {
            this.tokenType = tokenType;
            this.clientId = clientId;
            this.uuid = uuid;
            this.iat = System.currentTimeMillis() / 1000L + duration;
        }

        public long getIat() {
            return iat;
        }

        public void setIat(final long iat) {
            this.iat = iat;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(final String tokenType) {
            this.tokenType = tokenType;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(final String clientId) {
            this.clientId = clientId;
        }

        public String getUuid() {
            return uuid;
        }
    }

    public static class Response {
        public final String refreshToken;
        public final String accessToken;

        public Response(final String clientId, final String uuid) {
            final JWT refreshToken = new JWT(new Payload("refresh", clientId, uuid, 60 * 60 * 24 * 30)); // 30 days
            final JWT accessToken = new JWT(new Payload("access", clientId, uuid, 60 * 60 * 24)); // 1 day
            this.refreshToken = refreshToken.toString();
            this.accessToken = accessToken.toString();
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
        return Base64.getUrlEncoder().encodeToString(JWT.getMd().digest(input.getBytes()));
    }

    public final Header header;

    public final Payload payload;

    public final String token;

    public JWT(final Payload payload) {
        this.header = new Header();
        this.payload = payload;
        this.token = JWT.hash(
                String.format("%s.%s.%s",
                        JWT.base64UrlEncode(Main.GSON.toJson(this.header)),
                        JWT.base64UrlEncode(Main.GSON.toJson(this.payload)),
                        (Main.PREFERENCES.getSecret())));
    }

    @Override
    public String toString() {
        return this.token;
    }
}
