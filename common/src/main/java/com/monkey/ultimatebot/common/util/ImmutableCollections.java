package com.monkey.ultimatebot.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Java 8 stand-ins for List/Set/Map.of and copyOf. */
public final class ImmutableCollections {
    private ImmutableCollections() {}

    public static <E> List<E> copyOf(Collection<? extends E> elements) {
        return Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(elements, "elements")));
    }

    public static <E> Set<E> copyOf(Set<? extends E> elements) {
        return Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(elements, "elements")));
    }

    public static <K, V> Map<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(map, "map")));
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static final <E> List<E> listOf(E... elements) {
        Objects.requireNonNull(elements, "elements");
        List<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return Collections.unmodifiableList(list);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static final <E> Set<E> setOf(E... elements) {
        Objects.requireNonNull(elements, "elements");
        Set<E> set = new HashSet<>(Math.max(16, elements.length * 2));
        Collections.addAll(set, elements);
        return Collections.unmodifiableSet(set);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>(2);
        map.put(Objects.requireNonNull(k1, "k1"), v1);
        return Collections.unmodifiableMap(map);
    }

    public static <E> List<E> emptyList() {
        return Collections.emptyList();
    }

    public static <E> Set<E> emptySet() {
        return Collections.emptySet();
    }
}
