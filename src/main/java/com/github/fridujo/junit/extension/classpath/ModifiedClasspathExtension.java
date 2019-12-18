package com.github.fridujo.junit.extension.classpath;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ReflectionUtils;

public class ModifiedClasspathExtension implements InvocationInterceptor {

    private final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(ModifiedClasspathExtension.class);

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        if (invocationContext.getExecutable().isAnnotationPresent(ModifiedClasspath.class)) {
            doTheMagic(invocation, invocationContext, extensionContext);
        } else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        if (invocationContext.getExecutable().isAnnotationPresent(ModifiedClasspath.class)) {
            doTheMagic(invocation, invocationContext, extensionContext);
        } else {
            invocation.proceed();
        }
    }

    private void doTheMagic(Invocation<Void> invocation,
                            ReflectiveInvocationContext<Method> invocationContext,
                            ExtensionContext extensionContext) {
        silentlyInvokeOriginalMethod(invocation);

        ExtensionContext.Store store = extensionContext.getStore(namespace);
        ClasspathContext context = store.getOrComputeIfAbsent(ClasspathContext.class);

        ModifiedClasspath annotation = invocationContext.getExecutable().getAnnotation(ModifiedClasspath.class);
        ClassLoader modifiedClassLoader = Classpath.current(context)
            .removeJars(annotation.excludeJars())
            .removeGavs(annotation.excludeGavs())
            .replaceProductionCode(annotation.replaceProductionCode())
            .newClassLoader();

        ClassLoader currentThreadPreviousClassLoader = replaceCurrentThreadClassLoader(modifiedClassLoader);

        try {
            invokeMethodWithModifiedClasspath(
                invocationContext.getExecutable().getDeclaringClass().getName(),
                invocationContext.getExecutable().getName(),
                invocationContext.getArguments(),
                modifiedClassLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(currentThreadPreviousClassLoader);
        }
    }

    private ClassLoader replaceCurrentThreadClassLoader(ClassLoader modifiedClassLoader) {
        ClassLoader currentThreadPreviousClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(modifiedClassLoader);
        return currentThreadPreviousClassLoader;
    }

    private void invokeMethodWithModifiedClasspath(String className, String methodName, List<Object> arguments, ClassLoader classLoader) {
        final Class<?> testClass;
        try {
            testClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load test class [" + className + "] from modified classloader, verify that you did not exclude a path containing the test", e);
        }

        Object testInstance = ReflectionUtils.newInstance(testClass);
        Class[] parameterTypes = arguments.stream().map(o -> o.getClass()).collect(Collectors.toList()).toArray(new Class[0]);
        Optional<Method> method = ReflectionUtils.findMethod(testClass, methodName, parameterTypes);
        ReflectionUtils.invokeMethod(
            method.orElseThrow(() -> new IllegalStateException("No test method named " + methodName)),
            testInstance,
            arguments.toArray(new Object[0])
        );
    }

    /**
     * This is needed with this implementation.
     * As mentioned in {@link InvocationInterceptor} documentation, given invocation must be called exactly once.
     * However we are not interested in the outcome of this invocation, it is only a side-effect to get the whole shebang working.
     */
    private void silentlyInvokeOriginalMethod(Invocation<Void> invocation) {
        SystemState systemState = SystemState.backupAndSilence();
        try {
            invocation.proceed();
        } catch (Throwable t) {
        } finally {
            systemState.restore();
        }
    }
}
