package com.github.fridujo.junit.extension.classpath;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gav {
    private static final Pattern GAV_PATTERN = Pattern.compile("(?:(?<groupId>[^:]+):)?(?<artifactId>[^:]+)(?::(?<version>.+))?");
    private static final Pattern ABSOLUTE_GAV_PATTERN = Pattern.compile("(?<groupId>[^:]+):(?<artifactId>[^:]+):(?<version>.+)");
    public final String artifactId;
    public final String groupId;
    public final String version;
    private final Pattern jarPattern;

    public Gav(String artifactId, String groupId, String version) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.version = version;
        this.jarPattern = buildJarPattern(artifactId, groupId, version);
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    private static Pattern buildJarPattern(String artifactId, String groupId, String version) {
        if (groupId == null) {
            return Pattern.compile("(.*)" + Pattern.quote(artifactId) + "([^\\" + File.separatorChar + "]*)" + Pattern.quote(".jar"));
        }

        StringBuilder regex = new StringBuilder("(?:.*)")
            .append(Pattern.quote(groupId.replace('.', File.separatorChar) + File.separatorChar + artifactId + File.separatorChar));
        if (version != null) {
            regex.append(Pattern.quote(version + File.separatorChar + artifactId + "-" + version + ".jar"));
        } else {
            regex.append("(.*)").append(Pattern.quote(artifactId)).append("(.*)").append(Pattern.quote(".jar"));
        }
        return Pattern.compile(regex.toString());
    }

    public static Gav parse(String gav) {
        final Matcher matcher = GAV_PATTERN.matcher(gav.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a GAV expression");
        }
        return new Gav(matcher.group("artifactId"), matcher.group("groupId"), matcher.group("version"));
    }

    public static Gav parseAbsolute(String gav) {
        final Matcher matcher = ABSOLUTE_GAV_PATTERN.matcher(gav.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not an Absolute GAV expression");
        }
        return new Gav(matcher.group("artifactId"), matcher.group("groupId"), matcher.group("version"));
    }

    public boolean matchesJar(String rawJarPath) {
        return jarPattern.matcher(rawJarPath).matches();
    }

    public String toRelativePath() {
        return groupId.replace('.', File.separatorChar) + File.separatorChar + artifactId + File.separatorChar + version + File.separatorChar
            + artifactId + '-' + version + ".jar";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (groupId != null) {
            sb.append(groupId).append(':');
        }
        sb.append(artifactId);
        if (version != null) {
            sb.append(':').append(version);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gav gav = (Gav) o;
        return artifactId.equals(gav.artifactId) &&
            Objects.equals(groupId, gav.groupId) &&
            Objects.equals(version, gav.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, groupId, version);
    }
}
