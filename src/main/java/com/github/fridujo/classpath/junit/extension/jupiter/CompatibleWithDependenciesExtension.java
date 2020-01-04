package com.github.fridujo.classpath.junit.extension.jupiter;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.github.fridujo.classpath.junit.extension.Classpath;
import com.github.fridujo.classpath.junit.extension.GavReplacement;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import com.github.fridujo.classpath.junit.extension.utils.Streams;

class CompatibleWithDependenciesExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        CompatibilityTestWithClasspath annotation = context.getRequiredTestMethod().getAnnotation(CompatibilityTestWithClasspath.class);
        List<DependencyWithVersionRange> dependenciesWithVersionRange = Arrays.stream(annotation.dependencies())
            .map(DependencyWithVersionRange::parse)
            .collect(toList());

        BuildTool buildTool = BuildToolLocator.locate(context);
        Classpath currentClasspath = Classpath.current(buildTool);
        BuildToolLocator.store(context, currentClasspath.buildTool);

        List<List<GavReplacement>> replacementsCombinations = Streams.reduce(dependenciesWithVersionRange,
            emptyList(),
            (depSets, dependencyWithVersionRange) -> combine(depSets, dependencyWithVersionRange, currentClasspath));

        return Stream.concat(
            annotation.withCurrent() ? Stream.of(new CurrentClasspathInvocationContext()) : Stream.empty(),
            replacementsCombinations.stream()
                .map(gavReplacements -> new CompatibleWithDependenciesInvocationContext(gavReplacements))
        );
    }

    private List<List<GavReplacement>> combine(List<List<GavReplacement>> alreadyCombined, DependencyWithVersionRange dependencyWithVersionRange, Classpath currentClasspath) {
        if (alreadyCombined.isEmpty()) {
            return dependencyWithVersionRange.toGavReplacements(currentClasspath)
                .stream()
                .map(Collections::singletonList)
                .collect(toList());
        } else {
            List<List<GavReplacement>> newCombinations = new ArrayList<>();
            for (List<GavReplacement> existingCombination : alreadyCombined) {
                for (GavReplacement gavReplacement : dependencyWithVersionRange.toGavReplacements(currentClasspath)) {
                    List<GavReplacement> newCombination = new ArrayList<>(existingCombination);
                    newCombination.add(gavReplacement);
                    newCombinations.add(newCombination);
                }
            }
            return newCombinations;
        }
    }
}
