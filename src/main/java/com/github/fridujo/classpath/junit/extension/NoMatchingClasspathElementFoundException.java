package com.github.fridujo.classpath.junit.extension;

public class NoMatchingClasspathElementFoundException extends RuntimeException {

    public NoMatchingClasspathElementFoundException(Gav gav) {
        super(gav.toString() + " found no match in classpath");
    }
}
