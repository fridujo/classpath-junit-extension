package com.github.fridujo.junit.extension.classloader;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class SharedClassLoaderInjectionExtension implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            return parameterContext.getParameter().isAnnotationPresent(FromSharedClassLoader.class)
                && loadClassFromSharedClassLoader(parameterContext.getParameter().getType());
        } catch (ClassNotFoundException e) {
            throw new ParameterResolutionException("Parameter " + parameterContext + "] is marked with @" + FromSharedClassLoader.class.getSimpleName() + " but not present in the Shared Classloader");
        }
    }

    private boolean loadClassFromSharedClassLoader(Class<?> type) throws ClassNotFoundException {
        this.getClass().getClassLoader().getParent().loadClass(type.getName());
        return true;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getStore(extensionContext).getOrComputeIfAbsent(parameterContext.getParameter().getType());
    }

    private ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getStore(ExtensionContext.Namespace.create(SharedClassLoaderInjectionExtension.class, extensionContext.getRequiredTestClass()));
    }
}
