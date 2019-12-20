package com.github.fridujo.junit.extension.classpath;

import static java.util.Optional.empty;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import com.github.fridujo.junit.extension.classpath.buildtool.Artifact;
import com.github.fridujo.junit.extension.classpath.buildtool.BuildTool;
import com.github.fridujo.junit.extension.classpath.buildtool.BuildToolResolver;
import com.github.fridujo.junit.extension.classpath.buildtool.NoBuildToolFoundException;
import com.github.fridujo.junit.extension.classpath.utils.Streams;

public class ClasspathContext {

    private final BuildTool buildTool;

    public ClasspathContext(Set<PathElement> paths) {
        ServiceLoader<BuildToolResolver> serviceLoader = ServiceLoader.load(BuildToolResolver.class);
        Optional<BuildTool> buildTool = Streams.reduce(serviceLoader.iterator(), empty(), (obt1, obt2) -> obt1.isPresent() ? obt1 : obt2.resolve(paths));
        this.buildTool = buildTool.orElseThrow(NoBuildToolFoundException::new);
    }

    Set<Artifact> listDependencies(PathElement path) {
        return buildTool.listDependencies(path);
    }
}
