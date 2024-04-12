package com.github.fridujo.classpath.junit.extension.buildtool;

import com.github.fridujo.classpath.junit.extension.PathElement;

import java.util.Optional;
import java.util.Set;

public interface BuildToolResolver {

    Optional<BuildTool> resolve(Set<PathElement> classpath);
}
