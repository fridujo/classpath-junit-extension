package com.github.fridujo.classpath.junit.extension.utils;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class Streams {

    public static <T, U> U reduce(Iterable<T> iterable, U initialValue, BiFunction<U, T, U> accumulator) {
        return reduce(iterable.iterator(), initialValue, accumulator);
    }

    public static <T, U> U reduce(Stream<T> stream, U initialValue, BiFunction<U, T, U> accumulator) {
        return reduce(stream.iterator(), initialValue, accumulator);
    }

    public static <T, U> U reduce(Iterator<T> iterator, U initialValue, BiFunction<U, T, U> accumulator) {
        U value = initialValue;
        while (iterator.hasNext()) {
            value = accumulator.apply(value, iterator.next());
        }
        return value;
    }
}
