package com.github.fridujo.junit.extension.classpath;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GavTest {

    @ParameterizedTest
    @CsvSource({
        "junit-jupiter-params, , junit-jupiter-params,",
        "org.junit.jupiter:junit-jupiter-params, org.junit.jupiter, junit-jupiter-params,",
        "org.junit.jupiter:junit-jupiter-params:5.5.2, org.junit.jupiter, junit-jupiter-params, 5.5.2"
    })
    void parsing_works(String gavExpression, String groupId, String artifactId, String version) {
        final Gav gav = Gav.parse(gavExpression);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(gav.artifactId).isEqualTo(artifactId);
        softly.assertThat(gav.groupId).isEqualTo(groupId);
        softly.assertThat(gav.version).isEqualTo(version);
        softly.assertAll();
    }

    @ParameterizedTest
    @CsvSource({
        "junit-jupiter-params, /some/repo/org/junit/jupiter/junit-jupiter-params/1.3.RC2/junit-jupiter-params-1.3.RC2.jar, true",
        "junit-jupiter-params, /some/place/junit-jupiter-params.jar, true",
        "junit-jupiter-params, /some/place/junit-jupiter-params-1.3.RC2.jar, true",
        "junit-jupiter-params, /some/place/other-1.jar, false",

        "org.junit.jupiter:junit-jupiter-params, /some/repo/org/junit/jupiter/junit-jupiter-params/1.3.RC2/junit-jupiter-params-1.3.RC2.jar, true",
        "org.junit.jupiter:junit-jupiter-params, /some/repo/org/junit/jupiter/junit-jupiter-params/junit-jupiter-params-1.3.RC2.jar, true",
        "org.junit.jupiter:junit-jupiter-params, /some/place/junit-jupiter-params-1.3.RC2.jar, false",
        "org.junit.jupiter:junit-jupiter-params, /some/place/other-1.jar, false",

        "org.junit.jupiter:junit-jupiter-params:5.5.2, /some/repo/org/junit/jupiter/junit-jupiter-params/5.5.2/junit-jupiter-params-5.5.2.jar, true",
        "org.junit.jupiter:junit-jupiter-params:5.5.2, /some/repo/org/junit/jupiter/junit-jupiter-params/1.3.RC2/junit-jupiter-params-5.5.2.jar, false",
        "org.junit.jupiter:junit-jupiter-params:5.5.2, /some/repo/org/junit/jupiter/junit-jupiter-params/5.5.2/junit-jupiter-params-1.3.RC2.jar, false",
        "org.junit.jupiter:junit-jupiter-params:5.5.2, /some/repo/org/junit/jupiter/junit-jupiter-api/5.5.2/junit-jupiter-api-5.5.2.jar, false",
        "org.junit.jupiter:junit-jupiter-params:5.5.2, /some/place/junit-jupiter-params-5.5.2.jar, false",
        "org.junit.jupiter:junit-jupiter-params:5.5.2, /some/place/other-5.5.2.jar, false",
    })
    void gav_matches_against_raw_path(String gavExpression, String path, boolean expectedMatch) {
        final Gav gav = Gav.parse(gavExpression);

        assertThat(gav.matchesPath(path.replace('/', File.separatorChar))).isEqualTo(expectedMatch);
    }
}
