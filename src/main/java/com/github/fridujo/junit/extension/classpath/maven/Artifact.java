package com.github.fridujo.junit.extension.classpath.maven;

import com.github.fridujo.junit.extension.classpath.Gav;
import com.github.fridujo.junit.extension.classpath.PathElement;

public class Artifact {
    public final PathElement pathElement;
    public final Gav gav;

    public Artifact(PathElement pathElement, Gav gav) {
        this.pathElement = pathElement;
        this.gav = gav;
    }

    @Override
    public String toString() {
        return "Artifact{" +
            "pathElement=" + pathElement +
            ", gav=" + gav +
            '}';
    }
}
