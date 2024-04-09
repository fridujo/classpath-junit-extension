package com.github.fridujo.classpath.junit.extension.jupiter;

import com.github.fridujo.classpath.junit.extension.Classpath;
import com.github.fridujo.classpath.junit.extension.GavReplacement;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.List;

class CompatibleWithDependenciesExecutor extends AbstractClasspathExtension {

    private final String[] toRemove;
    private final String[] toAdd;

    CompatibleWithDependenciesExecutor(List<GavReplacement> gavReplacements) {
        toRemove = new String[gavReplacements.size()];
        toAdd = new String[gavReplacements.size()];
        for (int i = 0; i < gavReplacements.size(); i++) {
            toRemove[i] = gavReplacements.get(i).toRemove.toString();
            toAdd[i] = gavReplacements.get(i).toAdd.toString();
        }
    }

    @Override
    protected Classpath supplyClasspath(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, BuildTool buildTool) {
        return Classpath.current(buildTool)
            .removeDependencies(toRemove)
            .addDependencies(toAdd);
    }
}
