package com.github.fridujo.classpath.junit.extension.buildtool;

import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.PathElement;

import java.nio.file.Path;
import java.util.Set;

public interface BuildTool {

    Set<Artifact> listDependencies(PathElement path);

    Set<Artifact> downloadDependency(Gav absoluteGav);

    Gav toGav(PathElement pathElement);

    Path deleteLocalDependency(Gav gav);
}
