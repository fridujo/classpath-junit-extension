package com.github.fridujo.junit.extension.classpath;

import static java.util.Arrays.stream;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.github.fridujo.junit.extension.classpath.utils.Streams;

public class Classpath {

    public final Set<PathElement> pathElements;

    private final ClasspathContext context;

    private Classpath(Set<PathElement> pathElements, ClasspathContext context) {
        this.pathElements = Collections.unmodifiableSet(new TreeSet<>(pathElements));
        this.context = context;
    }

    public static Classpath current() {
        return current(new ClasspathContext());
    }

    public static Classpath current(ClasspathContext context) {
        String rawClasspath = System.getProperty("java.class.path");
        Set<PathElement> pathElements = stream(rawClasspath.split(File.pathSeparator))
            .map(PathElement::create)
            .collect(Collectors.toSet());
        return new Classpath(pathElements, context);
    }

    public ClassLoader newClassLoader() {
        ClassLoader parent = this.getClass().getClassLoader().getParent();
        final URLClassLoader urlClassLoader = new URLClassLoader(
            pathElements.stream().map(PathElement::toUrl).collect(Collectors.toList()).toArray(new URL[0]),
            parent);
        return urlClassLoader;
    }

    public Classpath removeJars(String[] excludeJars) {
        Set<PathElement> newPaths = new TreeSet<>(pathElements);
        Arrays.stream(excludeJars)
            .map(Gav::parse)
            .forEach(gav -> newPaths.removeIf(p -> p.matches(gav)));
        return new Classpath(newPaths, context);
    }

    public Classpath removeGavs(String[] gavDescriptions) throws NoMatchingClasspathElementFoundException {
        return Streams.reduce(stream(gavDescriptions), this, Classpath::removeGav);
    }

    public Classpath removeGav(String gavDescription) throws NoMatchingClasspathElementFoundException {
        Gav gav = Gav.parse(gavDescription);

        if (pathElements.stream().filter(pe -> pe.matches(gav)).count() == 0) {
            throw new NoMatchingClasspathElementFoundException(gav);
        }

        Classpath classpath = this;
        for (PathElement matchingPath : pathElements.stream().filter(pe -> pe.matches(gav)).collect(Collectors.toSet())) {
            classpath = classpath.removeGavWithMatchingPath(gav, matchingPath);
        }
        return classpath;
    }

    private Classpath removeGavWithMatchingPath(Gav gav, PathElement matchingPath) {
        Set<PathElement> newPaths = new TreeSet<>(pathElements);
        newPaths.removeIf(pe -> pe.matches(gav));

        Set<Gav> gavsToRemove = context.listDependencies(matchingPath);

        if (!gavsToRemove.isEmpty()) {
            Set<Gav> gavsToKeep = new HashSet<>();
            for (PathElement pe : pathElements) {
                if (!pe.matches(gav) && !gavsToRemove.stream().anyMatch(g -> pe.matches(g))) {
                    gavsToKeep.addAll(context.listDependencies(pe));
                }
            }

            gavsToRemove.removeAll(gavsToKeep);
            newPaths.removeIf(pe -> pe.matches(gavsToRemove));
        }
        return new Classpath(newPaths, context);
    }
}
