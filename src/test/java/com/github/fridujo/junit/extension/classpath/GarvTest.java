package com.github.fridujo.junit.extension.classpath;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.fridujo.junit.extension.classpath.utils.CommaSeparated;

class GarvTest {

    @ParameterizedTest
    @CsvSource({
        "junit-jupiter-params:5.5.2, , junit-jupiter-params, 5.5.2",
        "org.junit.jupiter:junit-jupiter-params:5.5.2, org.junit.jupiter, junit-jupiter-params, 5.5.2",
        "'org.junit.jupiter:junit-jupiter-params:[5.5.2, 5.2.0, 5.5.0]', org.junit.jupiter, junit-jupiter-params, '5.5.2, 5.2.0, 5.5.0'"
    })
    void parsing_works(String gavExpression, String groupId, String artifactId, @CommaSeparated List<String> versions) {
        final Garv gav = Garv.parse(gavExpression);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(gav.artifactId).isEqualTo(artifactId);
        softly.assertThat(gav.groupId).isEqualTo(groupId);
        softly.assertThat(gav.versions).isEqualTo(versions);
        //softly.assertThat(gav).hasToString(gavExpression);
        softly.assertAll();
    }
}
