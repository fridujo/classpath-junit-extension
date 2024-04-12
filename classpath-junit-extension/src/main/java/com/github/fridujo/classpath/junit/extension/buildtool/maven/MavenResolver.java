package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildToolResolver;

import java.util.Optional;
import java.util.Set;

public class MavenResolver implements BuildToolResolver {

    @Override
    public Optional<BuildTool> resolve(Set<PathElement> classpath) {
        return Optional.of(new Maven());
    }
}
