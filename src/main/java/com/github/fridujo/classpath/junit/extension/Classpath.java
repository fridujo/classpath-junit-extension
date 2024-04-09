package com.github.fridujo.classpath.junit.extension;

import com.github.fridujo.classpath.junit.extension.buildtool.Artifact;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildToolFactory;
import com.github.fridujo.classpath.junit.extension.utils.Streams;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class Classpath {

    public static final String SYSTEM_PROPERTY = "java.class.path";
    public final Set<PathElement> pathElements;

    public final BuildTool buildTool;

    private Classpath(Set<PathElement> pathElements, BuildTool buildTool) {
        this.pathElements = Collections.unmodifiableSet(new TreeSet<>(pathElements));
        this.buildTool = buildTool;
    }

    public static Classpath current(BuildTool buildTool) {
        String rawClasspath = System.getProperty(SYSTEM_PROPERTY);
        Set<PathElement> pathElements = stream(rawClasspath.split(File.pathSeparator))
            .map(PathElement::create)
            .collect(Collectors.toSet());
        return new Classpath(pathElements, buildTool != null ? buildTool : BuildToolFactory.buildFor(pathElements));
    }

    public static Classpath current() {
        return current(null);
    }

    public ClassLoader newClassLoader() {
        ClassLoader parent = this.getClass().getClassLoader().getParent();
        return new URLClassLoader(
            pathElements.stream().map(PathElement::toUrl).toArray(URL[]::new),
            parent);
    }

    public Classpath removeJars(String[] excludeJars) {
        Set<PathElement> newPaths = new TreeSet<>(pathElements);
        Arrays.stream(excludeJars)
            .map(Gav::parse)
            .forEach(gav -> newPaths.removeIf(p -> p.matches(gav)));
        return new Classpath(newPaths, buildTool);
    }

    public Classpath removeDependencies(String[] gavDescriptions) throws NoMatchingClasspathElementFoundException {
        return Streams.reduce(stream(gavDescriptions), this, Classpath::removeDependency);
    }

    public Classpath addDependencies(String[] absoluteGavDescriptions) {
        return Streams.reduce(stream(absoluteGavDescriptions), this, Classpath::addDependency);
    }

    public Classpath removeDependency(String gavDescription) throws NoMatchingClasspathElementFoundException {
        Gav gav = Gav.parse(gavDescription);

        if (pathElements.stream().noneMatch(pe -> pe.matches(gav))) {
            throw new NoMatchingClasspathElementFoundException(gav);
        }

        Classpath classpath = this;
        for (PathElement matchingPath : pathElements.stream().filter(pe -> pe.matches(gav)).collect(Collectors.toSet())) {
            classpath = classpath.removeDependencyWithMatchingPath(gav, matchingPath);
        }
        return classpath;
    }

    public Classpath addDependency(String absoluteGavDescription) {
        Gav gav = Gav.parseAbsolute(absoluteGavDescription);

        Set<PathElement> newPaths = new TreeSet<>(pathElements);
        Set<Artifact> artifactsToAdd = buildTool.downloadDependency(gav);
        newPaths.addAll(artifactsToAdd.stream().map(Artifact::path).collect(Collectors.toSet()));

        return new Classpath(newPaths, buildTool);
    }

    private Classpath removeDependencyWithMatchingPath(Gav gav, PathElement matchingPath) {
        Set<PathElement> newPaths = new TreeSet<>(pathElements);
        newPaths.removeIf(pe -> pe.matches(gav));

        Set<Artifact> artifactsToRemove = buildTool.listDependencies(matchingPath);

        if (!artifactsToRemove.isEmpty()) {
            Set<Artifact> gavsToKeep = new HashSet<>();
            for (PathElement pe : pathElements) {
                if (!pe.matches(gav) && artifactsToRemove.stream().noneMatch(a -> pe.matches(a.gav()))) {
                    gavsToKeep.addAll(buildTool.listDependencies(pe));
                }
            }

            artifactsToRemove.removeAll(gavsToKeep);
            Set<Gav> gavsToRemove = artifactsToRemove.stream().map(Artifact::gav).collect(Collectors.toSet());
            newPaths.removeIf(pe -> pe.matches(gavsToRemove));
        }
        return new Classpath(newPaths, buildTool);
    }
}
