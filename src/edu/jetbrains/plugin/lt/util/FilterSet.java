package edu.jetbrains.plugin.lt.util;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilterSet<T> {
    private Predicate<T> tPredicate;

    public FilterSet() {
        tPredicate = t -> true;
    }

    public void add(Predicate<T> predicate) {
        tPredicate = tPredicate.and(predicate);
    }

    public void add(Predicate<T>... predicates) {
        for (Predicate<T> predicate : predicates) add(predicate);
    }

    public Stream<T> filter(Stream<T> stream) {
        return stream.filter(tPredicate);
    }

    public Stream<T> filter(Collection<T> collection) {
        return filter(collection.stream());
    }
}
