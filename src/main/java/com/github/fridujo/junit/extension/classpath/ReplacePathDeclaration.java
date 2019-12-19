package com.github.fridujo.junit.extension.classpath;

public class ReplacePathDeclaration {
    public final Gav original;
    public final Gav replacement;

    public ReplacePathDeclaration(Gav original, Gav replacement) {
        this.original = original;
        this.replacement = replacement;
    }
}
