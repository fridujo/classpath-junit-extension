package com.github.fridujo.junit.extension.classpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class ClasspathExclusionTests {

    @Test
    void junit_extension_can_be_loaded() throws ClassNotFoundException {
        assertThat(Class.forName("org.junit.jupiter.api.extension.Extension")).isExactlyInstanceOf(Class.class);
    }

    @Test
    @ModifiedClasspath(excludeJars = "junit-jupiter-api")
    void junit_extension_cannot_be_loaded() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("org.junit.jupiter.api.extension.Extension"));
    }
}
