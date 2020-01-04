package com.github.fridujo.junit.extension.classpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.github.fridujo.junit.extension.classpath.jupiter.ModifiedClasspath;

class ClasspathAdditionTests {

    @Test
    void no_addition() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("ch.qos.logback.core.Appender"));
    }

    @Test
    @ModifiedClasspath(addDependencies = "ch.qos.logback:logback-classic:1.2.3")
    void logback_addition() throws ClassNotFoundException {
        assertThat(Class.forName("ch.qos.logback.core.Appender")).isInterface();
    }

    @Test
    @Order(2)
    @ModifiedClasspath(addDependencies = "ch.qos.logback:logback-classic:1.2.3")
    void same_addition_uses_cache() {
        assertTimeoutPreemptively(
            Duration.of(100, ChronoUnit.MILLIS),
            () -> Class.forName("ch.qos.logback.core.Appender").isInterface()
        );
    }
}
