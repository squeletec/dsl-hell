package fluent.api.model.impl;

import java.util.*;
import java.util.function.Supplier;

public class LazyMap<K, V> implements Map<K, V> {
    private final Lazy<Map<K, V>> lazy;


    public LazyMap(Supplier<Map<K, V>> lazy) {
        this.lazy = Lazy.lazy(lazy);
    }

    private Map<K, V> get() {
        return lazy.get();
    }

    @Override
    public int size() {
        return get().size();
    }

    @Override
    public boolean isEmpty() {
        return get().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return get().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return get().get(key);
    }

    @Override
    public V put(K key, V value) {
        return get().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return get().remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        get().putAll(m);
    }

    @Override
    public void clear() {
        get().clear();
    }

    @Override
    public Set<K> keySet() {
        return get().keySet();
    }

    @Override
    public Collection<V> values() {
        return get().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return get().entrySet();
    }
}
