package com.github.fridujo.classpath.junit.extension.buildtool;

import org.eclipse.aether.resolution.DependencyResolutionException;

public class RuntimeDependencyResolutionException extends RuntimeException {
    public RuntimeDependencyResolutionException(DependencyResolutionException e) {
        super(e);
    }

    public RuntimeDependencyResolutionException(String message) {
        super(message);
    }
}
