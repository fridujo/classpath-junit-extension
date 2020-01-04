package com.github.fridujo.classpath.junit.extension.buildtool;

import java.util.Set;

import com.github.fridujo.classpath.junit.extension.Gav;
import com.github.fridujo.classpath.junit.extension.PathElement;

public interface BuildTool {

    Set<Artifact> listDependencies(PathElement path);

    Set<Artifact> downloadDependency(Gav absoluteGav);
}
