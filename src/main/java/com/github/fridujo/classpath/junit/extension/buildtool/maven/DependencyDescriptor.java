package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import java.util.Set;

import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.buildtool.Artifact;

class DependencyDescriptor {

    final Gav gav;
    final Set<Artifact> dependencies;

    DependencyDescriptor(Gav gav, Set<Artifact> dependencies) {
        this.gav = gav;
        this.dependencies = dependencies;
    }
}
