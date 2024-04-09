package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import com.github.fridujo.classpath.junit.extension.Gav;

public record SimplePom(String groupId, String artifactId, String version, Parent parent) {

    public Gav toGav() {
        String resolvedGroupId = groupId;
        if (resolvedGroupId == null && parent != null) {
            resolvedGroupId = parent().groupId();
        }
        String resolvedVersion = version;
        if (resolvedVersion == null && parent != null) {
            resolvedVersion = parent().version();
        }
        return new Gav(artifactId, resolvedGroupId, resolvedVersion);
    }

    public record Parent(String groupId, String version) {
    }
}
