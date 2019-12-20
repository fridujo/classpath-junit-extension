package com.github.fridujo.junit.extension.classpath;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A GAV with multiple versions
 */
class Garv {

    private static final Pattern GARV_PATTERN = Pattern.compile("(?:(?<groupId>[^:]+):)?(?<artifactId>[^:]+)(?::(?<version>.+))?");

    public final String artifactId;
    public final String groupId;
    public final List<String> versions;

    private Garv(String artifactId, String groupId, List<String> versions) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.versions = versions;
    }

    public static Garv parse(String garv) {
        final Matcher matcher = GARV_PATTERN.matcher(garv.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a GARV expression");
        }
        if(matcher.group("version") == null) {
            throw new IllegalArgumentException("Not a GARV expression");
        }
        String version = matcher.group("version").trim();
        final List<String> versions;
        if(version.startsWith("[") && version.endsWith("]")) {
            versions = Arrays.stream(version.substring(1, version.length() - 1).split(",")).map(String::trim).collect(Collectors.toList());
        } else {
            versions = Collections.singletonList(version);
        }
        return new Garv(matcher.group("artifactId"), matcher.group("groupId"), versions);
    }
}
