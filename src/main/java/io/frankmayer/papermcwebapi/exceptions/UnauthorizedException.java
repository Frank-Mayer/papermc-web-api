package io.frankmayer.papermcwebapi.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(final String message) {
        super(message);
    }
}
