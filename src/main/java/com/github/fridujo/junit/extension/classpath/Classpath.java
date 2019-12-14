package com.github.fridujo.junit.extension.classpath;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Classpath {

    private final Set<PathElement> pathElements;

    public Classpath(Set<PathElement> pathElements) {
        this.pathElements = Collections.unmodifiableSet(new HashSet<>(pathElements));
    }

    public static Classpath current() {
        String rawClasspath = System.getProperty("java.class.path");
        Set<PathElement> pathElements = Arrays.stream(rawClasspath.split(File.pathSeparator))
            .map(PathElement::create)
            .collect(Collectors.toSet());
        return new Classpath(pathElements);
    }

    public ClassLoader newClassLoader() {
        ClassLoader parent = this.getClass().getClassLoader().getParent();
        final URLClassLoader urlClassLoader = new URLClassLoader(
            pathElements.stream().map(PathElement::toUrl).collect(Collectors.toList()).toArray(new URL[0]),
            parent);
        return urlClassLoader;
    }

    public Classpath removeJars(String[] gavDescriptions) {
        Set<PathElement> newPaths = new HashSet<>(pathElements);
        Arrays.stream(gavDescriptions)
            .map(Gav::parse)
            .forEach(p -> newPaths.removeIf(pe -> pe.matches(p)));
        return new Classpath(newPaths);
    }
}
