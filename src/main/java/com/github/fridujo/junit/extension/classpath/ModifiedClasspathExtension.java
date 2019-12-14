package com.github.fridujo.junit.extension.classpath;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ReflectionUtils;

public class ModifiedClasspathExtension implements InvocationInterceptor {

    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        silentlyInvokeOriginalMethod(invocation);
        final ModifiedClasspath annotation = invocationContext.getExecutable().getAnnotation(ModifiedClasspath.class);
        ClassLoader modifiedClassLoader = Classpath.current().removeJars(annotation.excludeJars()).newClassLoader();
        invokeMethodWithModifiedClasspath(
            invocationContext.getExecutable().getDeclaringClass().getName(),
            invocationContext.getExecutable().getName(),
            modifiedClassLoader);
    }

    private void invokeMethodWithModifiedClasspath(String className, String methodName, ClassLoader classLoader) {
        final Class<?> testClass;
        try {
            testClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load test class [" + className + "] from modified classloader", e);
        }

        Object testInstance = ReflectionUtils.newInstance(testClass);
        final Optional<Method> method = ReflectionUtils.findMethod(testClass, methodName);
        ReflectionUtils.invokeMethod(
            method.orElseThrow(() -> new IllegalStateException("No test method named " + methodName)),
            testInstance);
    }

    /**
     * This is needed with this implementation.
     * As mentioned in {@link InvocationInterceptor} documentation, given invocation must be called exactly once.
     * However we are not interested in the outcome of this invocation, it is only a side-effect to get the whole shebang working.
     */
    private void silentlyInvokeOriginalMethod(Invocation<Void> invocation) {
        try {
            invocation.proceed();
        } catch (Throwable t) {
        }
    }
}
