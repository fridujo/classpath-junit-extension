package com.github.fridujo.classpath.junit.extension.utils;

import java.util.Optional;

public class SystemVariables {

    public String get(String name) {
        return Optional.ofNullable(System.getProperty(name)).orElse(System.getenv(name));
    }
}
