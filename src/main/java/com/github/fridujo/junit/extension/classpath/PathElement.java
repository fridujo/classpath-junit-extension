package com.github.fridujo.junit.extension.classpath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

public class PathElement implements Comparable<PathElement> {
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
        return new PathElement(normalizedRawPath);
    }

    public Path toPath() {
        return Paths.get(rawPath);
    }

    public URL toUrl() {
        try {
            return new URL("file:" + (!rawPath.startsWith("/") ? "/" : "") + rawPath);
        } catch (MalformedURLException e) {
            // Surely dead code as MalformedURLException is raised when protocol is not recognised
            throw new IllegalStateException(e);
        }
    }

    public boolean matches(Gav gav) {
        return gav.matchesJar(rawPath);
    }

    public boolean matches(Collection<Gav> gavs) {
        return gavs.stream().anyMatch(gav -> gav.matchesJar(rawPath));
    }

    @Override
    public String toString() {
        return rawPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathElement that = (PathElement) o;
        return rawPath.equals(that.rawPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawPath);
    }

    @Override
    public int compareTo(PathElement o) {
        return rawPath.compareTo(o.rawPath);
    }

    public boolean exists() {
        return Files.exists(toPath());
    }
}
