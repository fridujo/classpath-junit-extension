package com.github.fridujo.junit.extension.classpath.buildtool;

import java.util.Objects;

import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;

public class Artifact {
    public final Gav gav;
    public final PathElement path;

    public Artifact(Gav gav, PathElement path) {
        this.gav = gav;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return gav.equals(artifact.gav) &&
            path.equals(artifact.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gav, path);
    }
}
