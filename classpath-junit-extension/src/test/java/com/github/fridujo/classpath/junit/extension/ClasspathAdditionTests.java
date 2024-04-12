package com.github.fridujo.classpath.junit.extension;

import com.github.fridujo.classpath.junit.extension.buildtool.maven.MavenResolver;
import com.github.fridujo.classpath.junit.extension.jupiter.ModifiedClasspath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class ClasspathAdditionTests {

    @BeforeAll
    static void beforeAll() {
        new MavenResolver().resolve(Collections.emptySet()).get().deleteLocalDependency(Gav.parse("org.apache.logging.log4j:log4j-api:2.23.1"));
    }

    @Test
    void no_addition() {
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("org.apache.logging.log4j.spi.ExtendedLogger"));
    }

    @Test
    @ModifiedClasspath(addDependencies = "org.apache.logging.log4j:log4j-api:2.23.1")
    void logback_addition() throws ClassNotFoundException {
        assertThat(Class.forName("org.apache.logging.log4j.spi.ExtendedLogger")).isInterface();
    }

    @Test
    @Order(2)
    @ModifiedClasspath(addDependencies = "org.apache.logging.log4j:log4j-api:2.23.1")
    void same_addition_uses_cache() {
        assertTimeoutPreemptively(
            Duration.of(100, ChronoUnit.MILLIS),
            () -> Class.forName("org.apache.logging.log4j.spi.ExtendedLogger").isInterface()
        );
    }
}
