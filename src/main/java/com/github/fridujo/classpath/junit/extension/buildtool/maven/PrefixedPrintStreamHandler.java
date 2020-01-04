package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import org.apache.maven.shared.invoker.PrintStreamHandler;

import com.github.fridujo.classpath.junit.extension.Configuration;

class PrefixedPrintStreamHandler extends PrintStreamHandler {

    private final String prefix;

    PrefixedPrintStreamHandler(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void consumeLine(String line) {
        if (Configuration.INSTANCE.verbose) {
            super.consumeLine(prefix + line);
        }
    }
}
