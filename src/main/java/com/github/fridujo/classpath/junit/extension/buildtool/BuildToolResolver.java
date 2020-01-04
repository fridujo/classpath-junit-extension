package com.github.fridujo.classpath.junit.extension.buildtool;

import java.util.Optional;
import java.util.Set;

import com.github.fridujo.classpath.junit.extension.PathElement;

public interface BuildToolResolver {

    Optional<BuildTool> resolve(Set<PathElement> classpath);
}
