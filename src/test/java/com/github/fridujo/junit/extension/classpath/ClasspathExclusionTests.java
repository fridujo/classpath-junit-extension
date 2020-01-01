package com.github.fridujo.junit.extension.classpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import com.github.fridujo.junit.extension.classpath.junit.ModifiedClasspath;

class ClasspathExclusionTests {

    @Test
    void no_exclusion() throws ClassNotFoundException {
        assertThat(Class.forName("org.junit.jupiter.api.extension.Extension")).isExactlyInstanceOf(Class.class);
    }

    @Test
    @ModifiedClasspath(excludeDependencies = "junit-jupiter")
    void exclusion_of_multiple_gavs_and_their_dependencies() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("org.junit.jupiter.api.extension.Extension"));

        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("org.junit.jupiter.engine.JupiterTestEngine"));
    }

    @Test
    @ModifiedClasspath(excludeDependencies = "guava:guava")
    void exclusion_of_one_gav_and_its_dependencies() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("com.google.common.collect.Maps"));
        // Transitive dependency is removed
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("com.google.errorprone.annotations.DoNotMock"));
    }

    @Test
    @ModifiedClasspath(excludeJars = "guava:guava")
    void exclusion_of_one_jar() throws ClassNotFoundException {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("com.google.common.collect.Maps"));
        // Transitive dependency is kept
        assertThat(Class.forName("com.google.errorprone.annotations.DoNotMock")).isExactlyInstanceOf(Class.class);
    }

    @Test
    @ModifiedClasspath(excludeJars = "guava:guava")
    void current_thread_classLoader_is_also_replaced() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Thread.currentThread().getContextClassLoader().loadClass("com.google.common.collect.Maps"));
    }

    @Test
    void excluding_non_matching_gav_throws() {
        assertThatExceptionOfType(NoMatchingClasspathElementFoundException.class)
            .isThrownBy(() -> Classpath.current().removeDependency("grId:not_existing:1.2-RC3"))
            .withMessage("grId:not_existing:1.2-RC3 found no match in classpath");
    }
}
