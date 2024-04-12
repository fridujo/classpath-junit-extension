package com.github.fridujo.classpath.junit.extension.jupiter;

import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

public class CurrentClasspathInvocationContext implements TestTemplateInvocationContext {

    @Override
    public String getDisplayName(int invocationIndex) {
        return "current";
    }
}
