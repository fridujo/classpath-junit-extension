package com.github.fridujo.classpath.junit.extension.buildtool;

import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.PathElement;

public record Artifact(Gav gav, PathElement path) implements Comparable<Artifact> {

    @Override
    public int compareTo(Artifact o) {
        return path.compareTo(o.path);
    }
}
