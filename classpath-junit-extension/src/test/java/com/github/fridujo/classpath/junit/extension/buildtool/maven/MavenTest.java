package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import com.github.fridujo.classpath.junit.extension.Classpath;
import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.buildtool.Artifact;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import com.github.fridujo.classpath.junit.extension.buildtool.RuntimeDependencyResolutionException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MavenTest {

    private static final Gav RABBITMQ_MOCK_GAV = Gav.parse("com.github.fridujo:rabbitmq-mock:1.0.10");

    @Test
    void listDependencies_resolves() {
        BuildTool maven = getMaven();
        Gav gav = Gav.parse("junit-jupiter-api");
        PathElement pathElement = Classpath.current().pathElements.stream().filter(pe -> pe.matches(gav)).findFirst().get();

        Set<Gav> deps = maven.listDependencies(pathElement).stream().map(Artifact::gav).collect(Collectors.toSet());

        assertThat(deps)
            .as("Dependencies of" + (Files.exists(pathElement.toPath()) ? "" : " not") + " existing " + pathElement)
            .extracting(Gav::getGroupId, Gav::getArtifactId)
            .containsOnly(
                tuple("org.junit.jupiter", "junit-jupiter-api"),
                tuple("org.junit.platform", "junit-platform-commons"),
                tuple("org.opentest4j", "opentest4j"),
                tuple("org.apiguardian", "apiguardian-api")
            );
    }

    @Test
    @Order(2)
    void downloadDependency_resolves() {
        BuildTool maven = getMaven();

        Path libraryRepository = maven.deleteLocalDependency(RABBITMQ_MOCK_GAV);
        assertThat(libraryRepository).doesNotExist();

        Set<Artifact> artifacts = maven.downloadDependency(RABBITMQ_MOCK_GAV);
        assertThat(artifacts).extracting(Artifact::gav).containsOnly(
            RABBITMQ_MOCK_GAV,
            Gav.parse("com.rabbitmq:amqp-client:5.6.0"),
            Gav.parse("org.slf4j:slf4j-api:1.7.25")
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

        assertThatExceptionOfType(RuntimeDependencyResolutionException.class)
            .isThrownBy(() -> maven.downloadDependency(Gav.parse("not_existing:lib:1.0.0")))
            .withMessageContaining("Could not find artifact not_existing:lib:jar:1.0.0 in central");
    }

    private BuildTool getMaven() {
        return new MavenResolver().resolve(Collections.emptySet()).get();
    }
}
