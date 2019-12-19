package com.github.fridujo.junit.extension.classpath;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.github.fridujo.junit.extension.classpath.maven.Artifact;
import com.github.fridujo.junit.extension.classpath.maven.Maven;

// Remove whole M2 initialization with existing path
// load instead default user locaRepository
// later if needed, read M2_HOME sysprop to get overriding if any
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

    public Stream<Artifact> download(PathElement path, Gav replacement) {
        return getMaven(path).orElseThrow(() -> new IllegalStateException("Maven not initialized")).download(replacement).stream();
    }
}
