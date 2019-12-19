package com.github.fridujo.junit.extension.classpath;

import java.util.Set;

public class NoMatchingClasspathElementFoundException extends RuntimeException {

    public NoMatchingClasspathElementFoundException(Gav gav) {
        super(gav.toString() + " found no match in classpath");
    }

    public NoMatchingClasspathElementFoundException(Set<Gav> gavs) {
        super(gavs.toString() + " found no match in classpath");
    }
}
