package com.github.fridujo.classpath.junit.extension.jupiter;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import com.github.fridujo.classpath.junit.extension.GavReplacement;

class CompatibleWithDependenciesInvocationContext implements TestTemplateInvocationContext {

    private final String displayName;
    private final List<GavReplacement> gavReplacements;

    CompatibleWithDependenciesInvocationContext(List<GavReplacement> gavReplacements) {
        this.displayName = computeDisplayName(gavReplacements);
        this.gavReplacements = gavReplacements;
    }

    private String computeDisplayName(List<GavReplacement> gavReplacements) {
        return gavReplacements.stream().map(gr -> gr.toAdd.toString()).collect(Collectors.joining(", ", "compatible with ", ""));
    }

    @Override
    public String getDisplayName(int invocationIndex) {
        return displayName;
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
        return singletonList(new CompatibleWithDependenciesExecutor(gavReplacements));
    }
}
