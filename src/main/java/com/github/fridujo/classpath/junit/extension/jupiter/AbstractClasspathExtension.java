package com.github.fridujo.classpath.junit.extension.jupiter;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ReflectionUtils;

import com.github.fridujo.classpath.junit.extension.Classpath;
import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;

abstract class AbstractClasspathExtension implements InvocationInterceptor {

    private final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(AbstractClasspathExtension.class);

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        intercept(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(InvocationInterceptor.Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) {
        intercept(invocation, invocationContext, extensionContext);
    }

    private void intercept(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
        invocation.skip();

        BuildTool buildTool = BuildToolLocator.locate(extensionContext);
        Classpath classpath = supplyClasspath(invocationContext, extensionContext, buildTool);
        BuildToolLocator.store(extensionContext, classpath.buildTool);

        invokeMethodWithModifiedClasspath(invocationContext, classpath);
    }

    protected abstract Classpath supplyClasspath(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, BuildTool buildTool);

    private void invokeMethodWithModifiedClasspath(ReflectiveInvocationContext<Method> invocationContext, Classpath classpath) {
        ClassLoader modifiedClassLoader = classpath.newClassLoader();

        ClassLoader currentThreadPreviousClassLoader = replaceCurrentThreadClassLoader(modifiedClassLoader);
        String previousClassPathProperty = replaceClassPathProperty(classpath);

        try {
            invokeMethodWithModifiedClasspath(
                invocationContext.getExecutable().getDeclaringClass().getName(),
                invocationContext.getExecutable().getName(),
                modifiedClassLoader);
        } finally {
            System.setProperty(Classpath.SYSTEM_PROPERTY, previousClassPathProperty);
            Thread.currentThread().setContextClassLoader(currentThreadPreviousClassLoader);
        }
    }

    private ClassLoader replaceCurrentThreadClassLoader(ClassLoader modifiedClassLoader) {
        ClassLoader currentThreadPreviousClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(modifiedClassLoader);
        return currentThreadPreviousClassLoader;
    }

    private String replaceClassPathProperty(Classpath classpath) {
        String previousClassPathProperty = System.getProperty(Classpath.SYSTEM_PROPERTY);
        System.setProperty(Classpath.SYSTEM_PROPERTY, classpath.pathElements.stream().map(PathElement::toString).collect(Collectors.joining(File.pathSeparator)));
        return previousClassPathProperty;
    }

    private void invokeMethodWithModifiedClasspath(String className, String methodName, ClassLoader classLoader) {
        final Class<?> testClass;
        try {
            testClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load test class [" + className + "] from modified classloader, verify that you did not exclude a path containing the test", e);
        }

        Object testInstance = ReflectionUtils.newInstance(testClass);
        final Optional<Method> method = ReflectionUtils.findMethod(testClass, methodName);
        ReflectionUtils.invokeMethod(
            method.orElseThrow(() -> new IllegalStateException("No test method named " + methodName)),
            testInstance);
    }
}
