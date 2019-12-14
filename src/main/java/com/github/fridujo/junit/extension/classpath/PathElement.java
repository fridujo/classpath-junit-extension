package com.github.fridujo.junit.extension.classpath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class PathElement {
    private final String rawPath;

    private PathElement(String rawPath) {
        this.rawPath = rawPath;
    }

    public static PathElement create(String rawPath) {
        String normalizedRawPath = rawPath;

        boolean directory = Files.isDirectory(Paths.get(rawPath));
        boolean alreadyHasTerminalSlash = rawPath.trim().endsWith(File.separator);
        if (directory && !alreadyHasTerminalSlash) {
            normalizedRawPath = normalizedRawPath.trim() + File.separator;
        }
        if (!normalizedRawPath.startsWith("/")) {
            normalizedRawPath = "/" + normalizedRawPath;
        }
        return new PathElement(normalizedRawPath);
    }

    public URL toUrl() {
        try {
            return new URL("file:" + rawPath);
        } catch (MalformedURLException e) {
            // Surely dead code as MalformedURLException is raised when protocol is not recognised
            throw new IllegalStateException(e);
        }
    }

    boolean matches(Pattern pattern) {
        return pattern.matcher(rawPath).matches();
    }

    @Override
    public String toString() {
        return rawPath;
    }
}
