package io.frankmayer.papermcwebapi.lua;

import java.util.Iterator;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class TableIterable implements Iterable<LuaValue> {
    private final LuaTable table;

    public TableIterable(final LuaTable table) {
        this.table = table;
    }

    @Override
    public Iterator<LuaValue> iterator() {
        return new TableIterator(this.table);
    }

    public static class TableIterator implements java.util.Iterator<LuaValue> {
        private final LuaTable table;
        private int index = 1;

        public TableIterator(final LuaTable table) {
            this.table = table;
        }

        @Override
        public boolean hasNext() {
            return !this.table.get(this.index + 1).isnil();
        }

        @Override
        public LuaValue next() {
            return this.table.get(++this.index);
        }
    }
}
