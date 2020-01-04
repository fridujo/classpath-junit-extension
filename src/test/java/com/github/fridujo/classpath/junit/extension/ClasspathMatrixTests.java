package com.github.fridujo.classpath.junit.extension;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.fridujo.classpath.junit.extension.jupiter.CompatibilityTestWithClasspath;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClasspathMatrixTests {

    private static final String HOLDER_PROP = "VERSIONS_TEST_HOLDER";

    @CompatibilityTestWithClasspath(dependencies = {
        "commons-io:[2.0.1, 2.4]",
        "commons-lang3:[3.0.1, 3.4]"
    })
    @Order(1)
    void mockito_and_assertj() {
        String commonsIoVersion = FileUtils.class.getPackage().getImplementationVersion();
        String commonsLangVersion = NumberUtils.class.getPackage().getImplementationVersion();
        appendCombinationInSystemProps(commonsIoVersion, commonsLangVersion);
    }

    @Test
    @Order(2)
    void checkDependenciesVersions() {
        Assertions.assertThat(System.getProperty(HOLDER_PROP).split(";"))
            .as("commons-io & commons-lang versions tuples")
            .contains(
                "2.0.1 3.0.1",
                "2.0.1 3.4",
                "2.4 3.0.1",
                "2.4 3.4"
            );
    }

    private void appendCombinationInSystemProps(String commonsIoVersion, String commonsLangVersion) {
        String property = System.getProperty(HOLDER_PROP);
        if (property == null) {
            property = "";
        } else {
            property += ";";
        }

        property += (commonsIoVersion + " " + commonsLangVersion);

        System.setProperty(HOLDER_PROP, property);
    }
}
