package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;

import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;
import com.github.fridujo.junit.extension.classpath.buildtool.Artifact;
import com.github.fridujo.junit.extension.classpath.buildtool.BuildTool;

class Maven extends MavenOperations implements BuildTool {

    private final Map<PathElement, Set<Artifact>> dependenciesCache = new LinkedHashMap<PathElement, Set<Artifact>>() {
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1000;
        }
    };
    private final Path mavenHome;
    private final Path localRepository;

    Maven(Path mavenHome, Path localRepository) {
        super(localRepository.toString());

        this.mavenHome = mavenHome;
        this.localRepository = localRepository;
    }

    public Set<Artifact> listDependencies(PathElement jarPath) {
        return dependenciesCache.computeIfAbsent(jarPath, p -> loadMavenProject(p)
            .map(mp -> mp.getDependencies().stream()
                .filter(d -> !"test".equals(d.getScope()) && !d.isOptional())
                .map(this::buildArtifact)
                .collect(Collectors.toSet())).orElseGet(Collections::emptySet));
    }

    private Artifact buildArtifact(Dependency d) {
        Gav gav = new Gav(d.getArtifactId(), d.getGroupId(), d.getVersion());
        return new Artifact(gav, PathElement.create(localRepository.toString() + gav.toRelativePath()));
    }
}
