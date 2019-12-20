package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.fridujo.junit.extension.classpath.Classpath;
import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;

class MavenTest {

    @Test
    void listDependencies_resolves() {
        Gav gav = Gav.parse("junit-jupiter-api");
        PathElement pathElement = Classpath.current().pathElements.stream().filter(pe -> pe.matches(gav)).findFirst().get();

        Set<Gav> deps = Maven.from(pathElement).get().listDependencies(pathElement);

        assertThat(deps).containsOnly(
            Gav.parse("org.junit.platform:junit-platform-commons:1.5.2"),
            Gav.parse("org.opentest4j:opentest4j:1.2.0"),
            Gav.parse("org.apiguardian:apiguardian-api:1.1.0")
        );
    }
}
