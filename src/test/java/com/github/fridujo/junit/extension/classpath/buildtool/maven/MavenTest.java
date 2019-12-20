package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.fridujo.junit.extension.classpath.Classpath;
import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;
import com.github.fridujo.junit.extension.classpath.buildtool.BuildTool;

class MavenTest {

    @Test
    void listDependencies_resolves() {
        Optional<BuildTool> maven = new MavenResolver().resolve(Collections.emptySet());
        Gav gav = Gav.parse("junit-jupiter-api");
        PathElement pathElement = Classpath.current().pathElements.stream().filter(pe -> pe.matches(gav)).findFirst().get();

        Set<Gav> deps = maven.get().listDependencies(pathElement).stream().map(a -> a.gav).collect(Collectors.toSet());

        assertThat(deps).containsOnly(
            Gav.parse("org.junit.platform:junit-platform-commons:1.5.2"),
            Gav.parse("org.opentest4j:opentest4j:1.2.0"),
            Gav.parse("org.apiguardian:apiguardian-api:1.1.0")
        );
    }
}
