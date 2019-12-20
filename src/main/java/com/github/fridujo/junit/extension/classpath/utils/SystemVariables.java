package com.github.fridujo.junit.extension.classpath.utils;

import java.util.Optional;

public class SystemVariables {

    public String get(String name) {
        return Optional.ofNullable(System.getProperty(name)).orElse(System.getenv(name));
    }
}
