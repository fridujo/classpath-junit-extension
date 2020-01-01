package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;

import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;
import com.github.fridujo.junit.extension.classpath.buildtool.Artifact;
import com.github.fridujo.junit.extension.classpath.buildtool.BuildTool;

class Maven extends MavenOperations implements BuildTool {

    final Path mavenHome;
    final Path localRepository;
    private final Map<PathElement, Set<Artifact>> dependenciesCache = new LinkedHashMap<PathElement, Set<Artifact>>() {
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1000;
        }
    };

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

    @Override
    public Set<Artifact> downloadDependency(Gav gav) {
        PathElement path = toPath(gav);
        if (!path.exists()) {
            Result result = executeGoal("dependency:get",
                "-DgroupId=" + gav.groupId,
                "-DartifactId=" + gav.artifactId,
                "-Dversion=" + gav.version);

            result.throwsOnError(() -> new IllegalStateException("Failed to download dependency " + gav));
        }

        Set<Artifact> elements = new LinkedHashSet<>();
        elements.add(new Artifact(gav, path));
        elements.addAll(listDependencies(path));
        return elements;
    }

    private Artifact buildArtifact(Dependency d) {
        Gav gav = new Gav(d.getArtifactId(), d.getGroupId(), d.getVersion());
        return new Artifact(gav, PathElement.create(localRepository.toString() + File.separatorChar + gav.toRelativePath()));
    }

    private PathElement toPath(Gav absoluteGav) {
        return PathElement.create(localRepository.toString() + File.separatorChar + absoluteGav.toRelativePath());
    }
}
