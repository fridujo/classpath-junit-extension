package com.github.fridujo.classpath.junit.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PathElementTest {

    @Test
    void toString_displays_the_path_for_debug_purposes() {
        assertThat(PathElement.create("/var/log/messages")).hasToString("/var/log/messages");
    }
}
