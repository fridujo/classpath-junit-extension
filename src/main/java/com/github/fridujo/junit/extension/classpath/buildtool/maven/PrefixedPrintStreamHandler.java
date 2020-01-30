package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import org.apache.maven.shared.invoker.PrintStreamHandler;

import com.github.fridujo.junit.extension.classpath.Configuration;

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
