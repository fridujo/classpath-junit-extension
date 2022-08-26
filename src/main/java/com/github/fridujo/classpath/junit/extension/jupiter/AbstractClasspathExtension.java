package com.github.fridujo.classpath.junit.extension.jupiter;

import com.github.fridujo.classpath.junit.extension.Classpath;
import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractClasspathExtension implements InvocationInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClasspathExtension.class);

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) {
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

        invokeMethodWithModifiedClasspath(invocationContext, classpath, extensionContext.getExecutableInvoker());
    }

    protected abstract Classpath supplyClasspath(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, BuildTool buildTool);

    private void invokeMethodWithModifiedClasspath(ReflectiveInvocationContext<Method> invocationContext, Classpath classpath, ExecutableInvoker executableInvoker) {
        ClassLoader modifiedClassLoader = classpath.newClassLoader();

        ClassLoader currentThreadPreviousClassLoader = replaceCurrentThreadClassLoader(modifiedClassLoader);
        String previousClassPathProperty = replaceClassPathProperty(classpath);

        try {
            invokeMethodWithModifiedClasspath(
                invocationContext.getExecutable(),
                modifiedClassLoader,
                executableInvoker);
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

    private void invokeMethodWithModifiedClasspath(Executable originalExecutable, ClassLoader classLoader, ExecutableInvoker executableInvoker) {
        String className = originalExecutable.getDeclaringClass().getName();
        final Class<?> testClass;
        try {
            testClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load test class [" + className + "] from modified classloader, verify that you did not exclude a path containing the test", e);
        }

        Constructor<?> constructor = testClass.getDeclaredConstructors()[0];
        Object testInstance = executableInvoker.invoke(constructor);

        String methodName = originalExecutable.getName();
        int parameterCount = originalExecutable.getParameterCount();
        List<Method> matchingMethods = ReflectionUtils.findMethods(testClass,
            m -> m.getName().equals(methodName) && m.getParameterCount() == parameterCount);
        if (matchingMethods.size() == 0) {
            throw new IllegalStateException("No test method named " + methodName);
        } else if (matchingMethods.size() > 1) {
            logger.warn(() -> "Multiple test methods with name " + methodName + " and " + parameterCount + " parameters");
        }

        executableInvoker.invoke(matchingMethods.get(0), testInstance);
    }
}
