package com.github.fridujo.classpath.junit.extension;

public class Configuration {

    public static final Configuration INSTANCE = build();

    public final boolean verbose;

    private Configuration(boolean verbose) {
        this.verbose = verbose;
    }

    private static Configuration build() {
        return new Configuration(Boolean.getBoolean("classpath.junit.extension.verbose"));
    }
}
