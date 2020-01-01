package com.github.fridujo.junit.extension.classpath.buildtool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;

class ArtifactTest {

    @Test
    void toString_displays_gav_and_path() {
        Gav gav = Gav.parse("junit");
        PathElement path = PathElement.create("/path/to/lib");
        Artifact artifact = new Artifact(gav, path);

        assertThat(artifact).hasToString(gav + " (" + path + ")");
    }

    @Test
    void compare_compares_only_path() {
        assertThat(new Artifact(Gav.parse("junit"), PathElement.create("/path/to/junit")))
            .isEqualByComparingTo(new Artifact(Gav.parse("other:lib"), PathElement.create("/path/to/junit")));
    }
}
