package dev.ramar.structs;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.function.Supplier;


/// A map that can be populated with raw values, or with
/// suppliers of those values. 
/// this gives you a nice trade off of for situations where
/// you don't *need* everything to be set, but you want it
/// so that when you .get(String key), it will always resolve correctly.
///
/// you can also build lazymaps, which is potentially handy!
/*
LazyMap<String, String> map = new LazyMap<>()
    .with("key", "value")
    .with("key2", () -> "value2");
;
*/

/// GPT-Made
public final class LazyMap<K, V> extends AbstractMap<K, V>
{
    private final LinkedHashMap<K, Supplier<V>> map;

    /* Constructors
    --===---------------------------------------------------------------------
    */

    public LazyMap()
    {
        this.map = new LinkedHashMap<>();
    }

    public LazyMap(int initialCapacity)
    {
        this.map = new LinkedHashMap<>(initialCapacity);
    }


    /* LazyMap API
    --===---------------------------------------------------------------------
    */

    public Supplier<V> put(K key, Supplier<V> supplier)
    {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(supplier, "supplier");

        map.put(key, supplier);
        return supplier;
    }


    public LazyMap<K, V> with(K key, Supplier<V> supplier)
    { this.put(key, supplier); return this; }

    public LazyMap<K, V> with(K key, V value)
    { this.put(key, value); return this; }


    public Set<Map.Entry<K, Supplier<V>>> supplierEntrySet()
    {
        return Collections.unmodifiableSet(map.entrySet());
    }

    /* Map Overrides
    --===---------------------------------------------------------------------
    */

    @Override
    public V put(K key, V value)
    {
        Supplier<V> previous = map.put(key, () -> value);
        if( previous == null )
            return null;

        return previous.get();
    }

    @Override
    public V get(Object key)
    {
        Supplier<V> supplier = map.get(key);
        if( supplier == null )
            return null;

        return supplier.get();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }

    @Override
    public V remove(Object key)
    {
        Supplier<V> removed = map.remove(key);
        if( removed == null )
            return null;

        return removed.get();
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public int size()
    {
        return map.size();
    }


    /* Entry Set (resolved view)
    --===---------------------------------------------------------------------
    */

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return new AbstractSet<>()
        {
            @Override
            public Iterator<Entry<K, V>> iterator()
            {
                Iterator<Entry<K, Supplier<V>>> it = map.entrySet().iterator();

                return new Iterator<>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<K, V> next()
                    {
                        Entry<K, Supplier<V>> entry = it.next();

                        return new SimpleEntry<>(
                            entry.getKey(),
                            entry.getValue() == null ? null : entry.getValue().get()
                        );
                    }

                    @Override
                    public void remove()
                    {
                        it.remove();
                    }
                };
            }

            @Override
            public int size()
            {
                return map.size();
            }
        };
    }
}