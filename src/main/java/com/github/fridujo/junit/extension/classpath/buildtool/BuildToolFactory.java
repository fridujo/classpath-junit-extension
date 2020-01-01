package com.github.fridujo.junit.extension.classpath.buildtool;

import static java.util.Optional.empty;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import com.github.fridujo.junit.extension.classpath.PathElement;
import com.github.fridujo.junit.extension.classpath.utils.Streams;

public class BuildToolFactory {

    public static BuildTool buildFor(Set<PathElement> paths) {
        ServiceLoader<BuildToolResolver> serviceLoader = ServiceLoader.load(BuildToolResolver.class);
        Optional<BuildTool> buildTool = Streams.reduce(serviceLoader.iterator(), empty(), (obt1, obt2) -> obt1.isPresent() ? obt1 : obt2.resolve(paths));
        return buildTool.orElseThrow(NoBuildToolFoundException::new);
    }
}
