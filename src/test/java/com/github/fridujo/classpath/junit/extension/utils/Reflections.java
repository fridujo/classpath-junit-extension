package com.github.fridujo.classpath.junit.extension.utils;

import java.lang.reflect.Field;

public class Reflections {

    public static void setFieldValue(Object o, String fieldName, Object value) {
        try {
            Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(o, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
