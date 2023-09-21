package io.frankmayer.papermcwebapi.gson;

import com.google.gson.TypeAdapter;

public abstract class GsonEnhancement<T extends Object> extends TypeAdapter<T> {
    public final Class<T> forType;

    public GsonEnhancement(final Class<T> forType) {
        this.forType = forType;
    }
}
