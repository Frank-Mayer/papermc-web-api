package io.frankmayer.papermcwebapi.utils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bukkit.OfflinePlayer;

import com.sun.net.httpserver.HttpExchange;

import io.frankmayer.papermcwebapi.HttpFrontend;
import io.frankmayer.papermcwebapi.Main;
import io.frankmayer.papermcwebapi.backend.Player;

public class JWT {
    public static class Header {
        private String typ = "JWT";
        private String alg = "HS512";

        public void setAlg(final String alg) {
            this.alg = alg;
        }

        public void setTyp(final String typ) {
            this.typ = typ;
        }

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
        private long exp;

        public Payload(final String tokenType, final String clientId, final String uuid, final long duration) {
            this.tokenType = tokenType;
            this.clientId = clientId;
            this.uuid = uuid;
            this.iat = System.currentTimeMillis();
            this.exp = this.iat + (duration * 1000);
        }

        public void setUuid(final String uuid) {
            this.uuid = uuid;
        }

        public long getExp() {
            return exp;
        }

        public void setExp(final long exp) {
            this.exp = exp;
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
        public final int refreshMaxAge = 60 * 60 * 24 * 30; // 30 days
        public final int accessMaxAge = 60 * 60 * 24; // 1 day
        public final String refreshExpires;
        public final String accessExpires;

        public Response(final String clientId, final String uuid) {
            final JWT refreshToken = new JWT(new Payload("refresh", clientId, uuid, this.refreshMaxAge));
            final JWT accessToken = new JWT(new Payload("access", clientId, uuid, this.accessMaxAge));
            this.refreshToken = refreshToken.toString();
            this.accessToken = accessToken.toString();

            if (JWT.dateFormat == null) {
                JWT.dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                JWT.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
            this.refreshExpires = JWT.dateFormat.format(refreshToken.payload.exp);
            this.accessExpires = JWT.dateFormat.format(accessToken.payload.exp);
        }
    }

    private static SimpleDateFormat dateFormat;

    private static MessageDigest md;

    public static OfflinePlayer processAuth(final HttpExchange t) {
        try {
            final Map<String, List<String>> headers = t.getRequestHeaders();
            if (!headers.containsKey("Cookie")) {
                Main.LOGGER.warning("No cookie header");
                return null;
            }
            final List<String> cookies = headers.get("Cookie");
            if (cookies.size() == 0) {
                Main.LOGGER.warning("Empty cookie header");
                return null;
            }

            JWT accessToken = null;
            JWT refreshToken = null;

            for (final String c : cookies) {
                final List<String> cookieParts = Str.split(c, ';');
                for (final String cookiePart : cookieParts) {
                    final List<String> cookie = Str.split(cookiePart, '=');
                    if (cookie.size() != 2) {
                        continue;
                    }
                    final String key = cookie.get(0).trim();
                    final String value = cookie.get(1).trim();
                    if (key.equalsIgnoreCase("access_token")) {
                        accessToken = new JWT(value);
                    } else if (key.equalsIgnoreCase("refresh_token")) {
                        refreshToken = new JWT(value);
                    }
                }
            }

            // maybe update the access token
            if (accessToken == null || accessToken.payload.exp < System.currentTimeMillis()) {
                if (refreshToken == null || refreshToken.payload.exp < System.currentTimeMillis()) {
                    return null;
                }
                final var response = new Response(refreshToken.payload.clientId,
                        refreshToken.payload.uuid);
                HttpFrontend.sendJWT(t, response);
                return Player.getBukkitOfflinePlayer(refreshToken.payload.uuid);
            }

            return Player.getBukkitOfflinePlayer(accessToken.payload.uuid);
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final Exception e) {
            Main.LOGGER.warning("Failed to process auth: " + e.getMessage());
            return null;
        }
    }

    public static MessageDigest getMd() {
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
        return Str.exclude(Base64.getUrlEncoder().encodeToString(input.getBytes()), '=');
    }

    private static String base64UrlDecode(final String token) throws IllegalArgumentException {
        return new String(Base64.getUrlDecoder().decode(token));
    }

    private static String hash(final String input) {
        return Str.exclude(Base64.getUrlEncoder().encodeToString(JWT.getMd().digest(input.getBytes())), '=');
    }

    public final Header header;

    public final Payload payload;

    public final String token;

    public JWT(final Payload payload) {
        this.header = new Header();
        this.payload = payload;
        this.token = this.makeToken();
    }

    public JWT(final String token) throws IllegalArgumentException {
        final String decoded = JWT.base64UrlDecode(token);

        // split into parts
        final List<String> tokenParts = Str.split(decoded, '.');
        if (tokenParts.size() != 3) {
            throw new IllegalArgumentException(
                    String.format("Invalid token format: %d parts, expected 3", tokenParts.size()));
        }

        // parse parts
        try {
            this.header = Main.GSON.fromJson(JWT.base64UrlDecode(tokenParts.get(0)), Header.class);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid token: invalid header");
        }
        try {
            this.payload = Main.GSON.fromJson(JWT.base64UrlDecode(tokenParts.get(1)), Payload.class);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid token: invalid payload");
        }
        try {
            this.token = JWT.base64UrlDecode(token);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid token: invalid base64 encoding");
        }

        // test
        final String testToken = this.makeToken();
        if (!testToken.equals(token)) {
            throw new IllegalArgumentException("Invalid token: signature mismatch");
        }
    }

    @Override
    public String toString() {
        return this.token;
    }

    private String makeToken() {
        final String h = JWT.base64UrlEncode(Main.GSON.toJson(this.header));
        final String p = JWT.base64UrlEncode(Main.GSON.toJson(this.payload));

        final String sign = JWT.hash(
                String.format("%s.%s.%s",
                        h, p,
                        (Main.PREFERENCES.getSecret())));

        return JWT.base64UrlEncode(
                String.format("%s.%s.%s",
                        h, p, sign));

    }
}
