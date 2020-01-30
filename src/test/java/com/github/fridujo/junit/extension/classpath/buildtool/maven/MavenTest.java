package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.fridujo.junit.extension.classpath.Classpath;
import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;
import com.github.fridujo.junit.extension.classpath.buildtool.Artifact;
import com.github.fridujo.junit.extension.classpath.buildtool.BuildTool;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MavenTest {

    private static final Gav RABBITMQ_MOCK_GAV = Gav.parse("com.github.fridujo:rabbitmq-mock:1.0.10");

    @Test
    void listDependencies_resolves() {
        BuildTool maven = getMaven();
        Gav gav = Gav.parse("junit-jupiter-api");
        PathElement pathElement = Classpath.current().pathElements.stream().filter(pe -> pe.matches(gav)).findFirst().get();

        Set<Gav> deps = maven.listDependencies(pathElement).stream().map(a -> a.gav).collect(Collectors.toSet());

        assertThat(deps)
            .as("Dependencies of" + (Files.exists(pathElement.toPath()) ? "" : " not") + " existing " + pathElement)
            .extracting(Gav::getGroupId, Gav::getArtifactId)
            .containsOnly(
                tuple("org.junit.platform", "junit-platform-commons"),
                tuple("org.opentest4j", "opentest4j"),
                tuple("org.apiguardian", "apiguardian-api")
            );
    }

    @Test
    @Order(2)
    void downloadDependency_resolves() {
        BuildTool maven = getMaven();

        Path libraryRepository = ((Maven) maven).localRepository.resolve("com").resolve("github").resolve("fridujo").resolve(RABBITMQ_MOCK_GAV.artifactId).resolve(RABBITMQ_MOCK_GAV.version);
        delete(libraryRepository);
        assertThat(libraryRepository).doesNotExist();

        Set<Artifact> artifacts = maven.downloadDependency(RABBITMQ_MOCK_GAV);
        assertThat(artifacts).extracting(a -> a.gav).containsOnly(
            RABBITMQ_MOCK_GAV,
            Gav.parse("com.rabbitmq:amqp-client:5.6.0")
        );
    }

    @Test
    @Order(3)
    void downloadDependency_caches() {
        BuildTool maven = getMaven();

        assertTimeoutPreemptively(
            Duration.of(100, ChronoUnit.MILLIS),
            () -> maven.downloadDependency(Gav.parse("com.github.fridujo:rabbitmq-mock:1.0.10"))
        );
    }

    @Test
    @Order(1)
    void downloadDependency_fails_when_not_existing() {
        BuildTool maven = getMaven();

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> maven.downloadDependency(Gav.parse("not_existing:lib:1.0.0")))
            .withMessage("Failed to download dependency not_existing:lib:1.0.0");
    }

    @Test
    void downloadDependency_fails_when_configuration_is_broken() {
        BuildTool maven = getMaven();

        String realMavenHome = System.getProperty("maven.home");
        System.setProperty("maven.home", Paths.get("").toAbsolutePath().toString());
        try {
            assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> maven.downloadDependency(Gav.parse("not_existing:lib:1.0.0")))
                .withCauseInstanceOf(MavenInvocationException.class);
        } finally {
            if (realMavenHome == null) {
                System.clearProperty("maven.home");
            } else {
                System.setProperty("maven.home", realMavenHome);
            }
        }
    }

    private BuildTool getMaven() {
        return new MavenResolver().resolve(Collections.emptySet()).get();
    }

    private void delete(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(
                        Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                        Path file, BasicFileAttributes attrs)
                        throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete: " + path, e);
        }
    }
}
