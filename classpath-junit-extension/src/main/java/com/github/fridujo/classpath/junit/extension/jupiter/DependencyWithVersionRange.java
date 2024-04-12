package com.github.fridujo.classpath.junit.extension.jupiter;

import com.github.fridujo.classpath.junit.extension.Classpath;
import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.GavReplacement;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class DependencyWithVersionRange {

    private static final Pattern DEP_PATTERN = Pattern.compile("(?:(?<groupId>[^:]+):)?(?<artifactId>[^:]+):\\[(?<versions>[^\\]]+)\\]");

    final Gav unversionnedGav;
    final List<String> versions;

    private DependencyWithVersionRange(String groupId, String artifactId, String versions) {
        this.unversionnedGav = new Gav(artifactId, groupId, null);
        this.versions = Arrays.asList(versions.split(",\\s*"));
    }

    static DependencyWithVersionRange parse(String description) {
        Matcher matcher = DEP_PATTERN.matcher(description);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a Dependency with range expression");
        }
        return new DependencyWithVersionRange(matcher.group("groupId"), matcher.group("artifactId"), matcher.group("versions"));
    }

    List<GavReplacement> toGavReplacements(Classpath currentClasspath) {
        Optional<Gav> absoluteGav = currentClasspath.pathElements.stream()
            .filter(pe -> pe.matches(unversionnedGav))
            .findFirst()
            .map(currentClasspath.buildTool::toGav);
        return versions.stream()
            .map(v -> new GavReplacement(
                unversionnedGav,
                absoluteGav.map(gav -> gav.withVersion(v)).orElse(unversionnedGav.withVersion(v))
            ))
            .collect(Collectors.toList());
    }
}
