package com.github.fridujo.classpath.junit.extension.buildtool;

import java.util.Objects;

import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.PathElement;

public class Artifact implements Comparable<Artifact> {
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

    @Override
    public String toString() {
        return gav + " (" + path + ")";
    }

    @Override
    public int compareTo(Artifact o) {
        return path.compareTo(o.path);
    }
}
