package com.github.fridujo.junit.extension.classpath.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

class FromCommaSeparatedConverter extends SimpleArgumentConverter {

    private static final Map<Class<?>, Supplier<Collector<? super String, ?, ? extends Collection<?>>>> ITERABLE_BUILDERS;

    static {
        Map<Class<?>, Supplier<Collector<? super String, ?, ? extends Collection<?>>>> builders = new LinkedHashMap<>();
        builders.put(List.class, Collectors::toList);
        builders.put(Set.class, () -> Collectors.toCollection(LinkedHashSet::new));
        ITERABLE_BUILDERS = Collections.unmodifiableMap(builders);
    }

    @Override
    public Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
        if (source instanceof String) {
            Collector<? super String, ?, ? extends Collection<?>> collectorSupplier = ITERABLE_BUILDERS.entrySet().stream()
                .filter(e -> e.getKey().isAssignableFrom(targetType))
                .map(e -> e.getValue())
                .findFirst().orElseThrow(() -> new ArgumentConversionException("No convertion for " + targetType))
                .get();
            return Arrays.stream(((String) source).split(",")).map(String::trim).collect(collectorSupplier);
        }
        throw new ArgumentConversionException("Cannot convert from object of type "
            + source.getClass().getName() + " to type " + targetType.getName());
    }
}
