package io.frankmayer.papermcwebapi.utils;

import java.util.concurrent.Callable;

import io.frankmayer.papermcwebapi.Main;

public class Cached<T> {
    private T value;
    private double lastUpdate = 0;
    private final double maxAge;
    private int failCount = 0;

    public Cached(final double maxAge) {
        this.maxAge = maxAge;
    }

    public T get(final Callable<T> fn) {
        final double now = System.currentTimeMillis();
        if (now - this.lastUpdate > this.maxAge) {
            try {
                this.value = fn.call();
                this.lastUpdate = now;
            } catch (final Exception e) {
                Main.LOGGER.warning("Failed to update cached value: " + e.getMessage());
                if ((++this.failCount) > 5) {
                    Main.panic(String.format("Failed to update cached value %d times", this.failCount));
                }
            }
        }
        return this.value;
    }
}
