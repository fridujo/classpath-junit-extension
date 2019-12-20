package com.github.fridujo.junit.extension.classpath;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.params.ParameterizedTest;

import com.github.fridujo.junit.extension.classpath.junit.CompatibleWithDependencies;
import com.github.fridujo.junit.extension.classpath.junit.DependenciesInfo;

public class DependenciesMatrixTests {

    @CompatibleWithDependencies(value = {
        "junit-jupiter*:[5.2.0, 5.5.0]"
    }, current = true)
    void assertEquals_is_available_in_previous_versions_of_junit_jupiter(DependenciesInfo dependenciesInfo) {
        assertThat(ParameterizedTest.class.getPackage().getSpecificationVersion())
            .as("junit-jupiter-params version")
            .isEqualTo(dependenciesInfo.getVersion("junit-jupiter"));

        assertThat(Test.class.getPackage().getSpecificationVersion())
            .as("junit-jupiter-api version")
            .isEqualTo(dependenciesInfo.getVersion("junit-jupiter"));

        assertThat(JupiterTestEngine.class.getPackage().getSpecificationVersion())
            .as("junit-jupiter-engine version")
            .isEqualTo(dependenciesInfo.getVersion("junit-jupiter"));
    }
}
