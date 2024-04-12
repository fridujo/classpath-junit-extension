package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.buildtool.Artifact;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import com.github.fridujo.classpath.junit.extension.buildtool.RuntimeDependencyResolutionException;
import com.github.fridujo.classpath.junit.extension.utils.PathUtils;
import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtime;
import eu.maveniverse.maven.mima.context.Runtimes;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

class Maven implements BuildTool {

    final Path mavenHome;
    final Path localRepository;
    private final Logger logger = LoggerFactory.getLogger(Maven.class);
    private final Runtime runtime = Runtimes.INSTANCE.getRuntime();
    private final ContextOverrides overrides = ContextOverrides.create().withUserSettings(true).build();
    private final DependencySelector dependencySelector = new AndDependencySelector(
        new ScopeDependencySelector("test"),
        new OptionalDependencySelector(),
        new ExclusionDependencySelector()
    );

    public Maven() {
        try (Context context = runtime.create(overrides)) {
            mavenHome = context.mavenSystemHome().basedir();
            localRepository = context.repositorySystemSession().getLocalRepository().getBasedir().toPath();
        }
    }

    private static Optional<Path> getPomPath(PathElement jarPath) {
        String rawPath = jarPath.toPath().toString();
        if (!rawPath.endsWith(".jar")) {
            return Optional.empty();
        }
        Path pomPath = Paths.get(rawPath.substring(0, rawPath.length() - 4) + ".pom");
        if (!Files.exists(pomPath)) {
            return Optional.empty();
        }
        return Optional.of(pomPath);
    }

    @Override
    public Set<Artifact> listDependencies(PathElement jarPath) {
        Gav gav = toGav(jarPath);
        if (gav == null) {
            return emptySet();
        }
        return downloadDependency(gav);
    }

    @Override
    public Set<Artifact> downloadDependency(Gav gav) {
        DefaultArtifact artifact = new DefaultArtifact(gav.groupId + ":" + gav.artifactId + ":" + gav.version);
        return resolveDependencies(artifact);
    }

    @Override
    public Gav toGav(PathElement jarPath) {
        Optional<Path> pomPath = getPomPath(jarPath);
        if (pomPath.isEmpty()) {
            return null;
        }
        ObjectMapper xmlMapper = new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            SimplePom simplePom = xmlMapper.readValue(pomPath.get().toFile(), SimplePom.class);
            return simplePom.toGav();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Path deleteLocalDependency(Gav gav) {
        Path localPath = localRepository;
        for (String groupIdFragment : gav.groupId.split("\\.")) {
            localPath = localPath.resolve(groupIdFragment);
        }
        localPath = localPath.resolve(gav.artifactId);
        if (gav.version != null) {
            localPath = localPath.resolve(gav.version);
        }
        PathUtils.delete(localPath);
        return localPath;
    }

    private Set<Artifact> resolveDependencies(org.eclipse.aether.artifact.Artifact artifact) {
        try (Context context = runtime.create(overrides)) {
            Dependency dependency = new Dependency(artifact, "runtime");
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(dependency);
            collectRequest.setRepositories(context.remoteRepositories());
            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setCollectRequest(collectRequest);

            ((DefaultRepositorySystemSession) context.repositorySystemSession()).setDependencySelector(dependencySelector);

            DependencyNode rootNode = context.repositorySystem()
                .resolveDependencies(context.repositorySystemSession(), dependencyRequest)
                .getRoot();
            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            rootNode.accept(nlg);
            return nlg.getDependencies(false).stream()
                .map(d -> new Artifact(
                    new Gav(
                        d.getArtifact().getArtifactId(),
                        d.getArtifact().getGroupId(),
                        d.getArtifact().getVersion()),
                    PathElement.create(d.getArtifact().getFile().getAbsolutePath())))
                .collect(Collectors.toSet());
        } catch (DependencyResolutionException e) {
            throw new RuntimeDependencyResolutionException(e);
        }
    }

    @Override
    public String toString() {
        return "Maven{" +
            "mavenHome=" + mavenHome +
            ", localRepository=" + localRepository +
            '}';
    }
}
