package com.github.fridujo.junit.extension.classpath.buildtool;

import java.util.Optional;
import java.util.Set;

import com.github.fridujo.junit.extension.classpath.PathElement;

public interface BuildToolResolver {

    Optional<BuildTool> resolve(Set<PathElement> classpath);
}
