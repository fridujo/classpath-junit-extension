package com.github.fridujo.junit.extension.classpath.buildtool;

import java.util.Set;

import com.github.fridujo.junit.extension.classpath.PathElement;

public interface BuildTool {

    Set<Artifact> listDependencies(PathElement path);
}