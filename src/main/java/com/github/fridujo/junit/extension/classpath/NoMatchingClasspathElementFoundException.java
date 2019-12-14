package com.github.fridujo.junit.extension.classpath;

public class NoMatchingClasspathElementFoundException extends RuntimeException {

    public NoMatchingClasspathElementFoundException(Gav gav) {
        super(gav.toString() + " found no match in classpath");
    }
}
