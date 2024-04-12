package com.github.fridujo.classpath.junit.extension.jupiter;

import com.github.fridujo.classpath.junit.extension.Classpath;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

class ModifiedClasspathExtension extends AbstractClasspathExtension {

    @Override
    protected Classpath supplyClasspath(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, BuildTool buildTool) {
        ModifiedClasspath annotation = invocationContext.getExecutable().getAnnotation(ModifiedClasspath.class);
        Classpath classpath = Classpath.current(buildTool)
            .removeJars(annotation.excludeJars())
            .removeDependencies(annotation.excludeDependencies())
            .addDependencies(annotation.addDependencies());

        return classpath;
    }
}
