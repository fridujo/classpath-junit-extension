package com.github.fridujo.junit.extension.classpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import com.github.fridujo.junit.extension.classpath.junit.RemoveDependencies;

class ClasspathExclusionTests {

    @Test
    void no_exclusion() throws ClassNotFoundException {
        assertThat(Class.forName("org.junit.jupiter.api.extension.Extension")).isExactlyInstanceOf(Class.class);
    }

    @Test
    @RemoveDependencies(gavs = "junit-jupiter")
    void exclusion_of_multiple_gavs_and_their_dependencies() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("org.junit.jupiter.api.extension.Extension"));

        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("org.junit.jupiter.engine.JupiterTestEngine"));
    }

    @Test
    @RemoveDependencies(gavs = "guava:guava")
    void exclusion_of_one_gav_and_its_dependencies() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("com.google.common.collect.Maps"));
        // Transitive dependency is removed
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("com.google.errorprone.annotations.DoNotMock"));
    }

    @Test
    @RemoveDependencies(jars = "guava:guava")
    void exclusion_of_one_jar() throws ClassNotFoundException {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("com.google.common.collect.Maps"));
        // Transitive dependency is kept
        assertThat(Class.forName("com.google.errorprone.annotations.DoNotMock")).isExactlyInstanceOf(Class.class);
    }

    @Test
    @RemoveDependencies(jars = "guava:guava")
    void current_thread_classLoader_is_also_replaced() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Thread.currentThread().getContextClassLoader().loadClass("com.google.common.collect.Maps"));
    }

    @Test
    void excluding_non_matching_gav_throws() {
        assertThatExceptionOfType(NoMatchingClasspathElementFoundException.class)
            .isThrownBy(() -> Classpath.current().removeGav("grId:not_existing:1.2-RC3"))
            .withMessage("grId:not_existing:1.2-RC3 found no match in classpath");
    }
}
