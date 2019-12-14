package com.github.fridujo.junit.extension.classpath;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.github.fridujo.junit.extension.classpath.maven.Maven;

class ClasspathContext {

    private Maven maven;

    Optional<Maven> getMaven(PathElement path) {
        if (maven == null) {
            maven = Maven.from(path).orElse(null);
        }
        return Optional.ofNullable(maven);
    }

    Set<Gav> listDependencies(PathElement path) {
        return getMaven(path).map(m -> m.listDependencies(path)).orElse(Collections.emptySet());
    }
}
