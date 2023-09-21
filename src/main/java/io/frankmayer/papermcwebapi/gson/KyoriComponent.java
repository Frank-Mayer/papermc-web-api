package io.frankmayer.papermcwebapi.gson;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

public class KyoriComponent extends GsonEnhancement<net.kyori.adventure.text.Component> {
    public KyoriComponent() {
        super(Component.class);
    }

    private static final JSONComponentSerializer serializer = JSONComponentSerializer.json();

    @Override
    public void write(JsonWriter out, Component value) throws IOException {
        out.value(serializer.serialize(value));
    }

    @Override
    public Component read(JsonReader in) throws IOException {
        return serializer.deserialize(in.nextString());
    }
}
