package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.buildtool.Artifact;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;

class Maven extends MavenOperations implements BuildTool {

    final Path mavenHome;
    final Path localRepository;
    private final Map<PathElement, DependencyDescriptor> dependenciesCache = new LinkedHashMap<PathElement, DependencyDescriptor>() {
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
        return getDependencyDescriptor(jarPath).dependencies;
    }

    private DependencyDescriptor getDependencyDescriptor(PathElement jarPath) {
        return dependenciesCache.computeIfAbsent(jarPath, p -> {
            Optional<Model> model = loadMavenProject(p);
            Gav gav = model.map(m -> new Gav(m.getArtifactId(), m.getGroupId(), m.getVersion())).orElse(null);
            Set<Artifact> artifacts = model
                .map(mp -> mp.getDependencies().stream()
                    .filter(d -> !"test".equals(d.getScope()) && !d.isOptional())
                    .map(this::buildArtifact)
                    .collect(Collectors.toSet())).orElseGet(Collections::emptySet);
            return new DependencyDescriptor(gav, artifacts);
        });
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

    @Override
    public Gav toGav(PathElement pathElement) {
        return getDependencyDescriptor(pathElement).gav;
    }

    private Artifact buildArtifact(Dependency d) {
        Gav gav = new Gav(d.getArtifactId(), d.getGroupId(), d.getVersion());
        return new Artifact(gav, PathElement.create(localRepository.toString() + File.separatorChar + gav.toRelativePath()));
    }

    private PathElement toPath(Gav absoluteGav) {
        return PathElement.create(localRepository.toString() + File.separatorChar + absoluteGav.toRelativePath());
    }

    @Override
    public String toString() {
        return "Maven{" +
            "mavenHome=" + mavenHome +
            ", localRepository=" + localRepository +
            ", dependenciesCache=" + dependenciesCache +
            '}';
    }
}
