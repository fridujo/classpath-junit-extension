package com.github.fridujo.classpath.junit.extension;

public class GavReplacement {
    public final Gav toRemove;
    public final Gav toAdd;

    public GavReplacement(Gav toRemove, Gav toAdd) {
        this.toRemove = toRemove;
        this.toAdd = toAdd;
    }
}
