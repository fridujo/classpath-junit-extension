package com.github.fridujo.junit.extension.classpath.maven;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;

/**
 * To put in cache:
 * <ul>
 *     <li>dependencies({@code Set<Artifact>}) by Artifact</li>
 *     <li>Maven home (M2_HOME or set after CLI lookup)</li>
 *     <li>Local repository (~/.m2/repository if exists, mvn</li>
 * </ul>
 */
public class Maven extends MavenOperations {

    private final Map<PathElement, Set<Gav>> dependenciesCache = new LinkedHashMap<PathElement, Set<Gav>>() {
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1000;
        }
    };
    private final String localRepo;

    public Maven(String localRepo) {
        super(localRepo);
        this.localRepo = localRepo;
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


    public Set<Artifact> download(Gav gav) {
        PathElement path = toPath(gav);
        if (!path.exists()) {
            InvocationRequest request = new DefaultInvocationRequest();
            //request.setPomFile( new File( "/path/to/pom.xml" ) );
            request.setGoals(Arrays.asList("dependency:get",
                "-DgroupId=" + gav.groupId,
                "-DartifactId=" + gav.artifactId,
                "-Dversion=" + gav.version));
            Path pomPath = Paths.get("").toAbsolutePath().resolve("pom.xml");
            if (Files.exists(pomPath)) {
                // Useful to lookup for declared remote repositories (other than central)
                request.setPomFile(pomPath.toFile());
            }

            Invoker invoker = new DefaultInvoker();

            try {
                InvocationResult result = invoker.execute(request);
                if (result.getExitCode() != 0) {
                    throw new IllegalStateException("Failed to download dependency" + gav);
                }
            } catch (MavenInvocationException e) {
                throw new RuntimeException(e);
            }
        }

        Set<Gav> dependencies = listDependencies(path);
        Set<Artifact> elements = new LinkedHashSet<>();
        elements.add(new Artifact(path, gav));
        dependencies.stream()
            .map(g -> new Artifact(toPath(g), g))
            .forEach(elements::add);
        return elements;
    }

    private PathElement toPath(Gav absoluteGav) {
        return PathElement.create(localRepo + File.separatorChar + absoluteGav.toRelativePath());
    }
}
