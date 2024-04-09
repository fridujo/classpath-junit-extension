package com.github.fridujo.classpath.junit.extension;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathElementTest {

    @Test
    void toString_displays_the_path_for_debug_purposes() {
        assertThat(PathElement.create("/var/log/messages")).hasToString("/var/log/messages");
    }
}
