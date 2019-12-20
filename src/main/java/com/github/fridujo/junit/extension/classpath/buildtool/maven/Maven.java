package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;

import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;

public class Maven extends MavenOperations {

    private final Map<PathElement, Set<Gav>> dependenciesCache = new LinkedHashMap<PathElement, Set<Gav>>() {
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1000;
        }
    };

    public Maven(String localRepo) {
        super(localRepo);
    }

    public static Optional<Maven> from(PathElement jarPath) {
        return getNonEffectiveModel(jarPath)
            .map(nonEffectiveModel -> new Maven(deduceLocalRepo(jarPath, nonEffectiveModel)));
    }

    private static String deduceLocalRepo(PathElement jarPath, Model nonEffectiveModel) {
        String rawPath = jarPath.toPath().toString();
        String groupId = getGroupId(nonEffectiveModel);
        String version = getVersion(nonEffectiveModel);
        String mavenInternalStructure = groupId.replace('.', File.separatorChar) + File.separatorChar + nonEffectiveModel.getArtifactId() + File.separatorChar + version + File.separatorChar;

        int i = rawPath.indexOf(mavenInternalStructure);
        if (i < 0) {
            throw new IllegalStateException("Loaded a Maven dependency outside a repository");
        }

        return rawPath.substring(0, i);
    }

    public Set<Gav> listDependencies(PathElement jarPath) {
        return dependenciesCache.computeIfAbsent(jarPath, p -> loadMavenProject(p)
            .map(mp -> mp.getDependencies().stream()
                .filter(d -> !"test".equals(d.getScope()) && !d.isOptional())
                .map(d -> new Gav(d.getArtifactId(), d.getGroupId(), d.getVersion()))
                .collect(Collectors.toSet())).orElseGet(Collections::emptySet));
    }
}
