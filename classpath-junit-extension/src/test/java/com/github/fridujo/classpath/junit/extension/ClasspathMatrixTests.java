package com.github.fridujo.classpath.junit.extension;

import com.github.fridujo.classpath.junit.extension.jupiter.CompatibilityTestWithClasspath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClasspathMatrixTests {

    private static final String HOLDER_PROP = "VERSIONS_TEST_HOLDER";

    @CompatibilityTestWithClasspath(dependencies = {
        "slf4j-api:[2.0.1, 2.0.4, 2.0.12]",
        "jackson-annotations:[2.15.0, 2.16.0]"
    })
    @Order(1)
    void mockito_and_assertj() {
        String slf4jVersion = getClassPackage("org.slf4j.event.LoggingEvent").getImplementationVersion();
        String jacksonVersion = getClassPackage("com.fasterxml.jackson.annotation.JacksonAnnotationValue").getImplementationVersion();
        appendCombinationInSystemProps(slf4jVersion, jacksonVersion);
    }

    private Package getClassPackage(String className) {
        try {
            return Class.forName(className).getPackage();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void checkDependenciesVersions() {
        Assertions.assertThat(System.getProperty(HOLDER_PROP).split(";"))
            .as("slf4j-api & jackson-annotations versions tuples")
            .contains(
                "2.0.12 2.17.0",
                "2.0.1 2.15.0",
                "2.0.1 2.16.0",
                "2.0.4 2.15.0",
                "2.0.4 2.16.0",
                "2.0.12 2.15.0",
                "2.0.12 2.16.0"
            );
    }

    private void appendCombinationInSystemProps(String slf4jVersion, String jacksonVersion) {
        String property = System.getProperty(HOLDER_PROP);
        if (property == null) {
            property = "";
        } else {
            property += ";";
        }

        property += (slf4jVersion + " " + jacksonVersion);

        System.setProperty(HOLDER_PROP, property);
    }
}
