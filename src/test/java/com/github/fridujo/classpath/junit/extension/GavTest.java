package com.github.fridujo.classpath.junit.extension;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
        softly.assertThat(gav).hasToString(gavExpression);
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

        assertThat(gav.matchesJar(path.replace('/', File.separatorChar))).isEqualTo(expectedMatch);
    }

    @ParameterizedTest
    @CsvSource({
        "''",
        "'  '"
    })
    void gav_cannot_be_blank(String gavExpression) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> Gav.parse(gavExpression))
            .withMessage("Not a GAV expression");
    }

    @Test
    void absolute_gav_has_all_info() {
        Gav absoluteGav = Gav.parseAbsolute("org.junit.jupiter:junit-jupiter-params:5.5.2");

        assertThat(absoluteGav.groupId).isEqualTo("org.junit.jupiter");
        assertThat(absoluteGav.artifactId).isEqualTo("junit-jupiter-params");
        assertThat(absoluteGav.version).isEqualTo("5.5.2");
    }

    @ParameterizedTest
    @CsvSource({
        "junit-jupiter-params",
        "org.junit.jupiter:junit-jupiter-params",
    })
    void absolute_gav_fails_to_parse_is_groupId_or_version_is_missing(String partialGav) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> Gav.parseAbsolute(partialGav))
            .withMessage("Not an Absolute GAV expression");
    }
}
