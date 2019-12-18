package com.github.fridujo.junit.extension.classpath;

import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.fridujo.junit.extension.classloader.FromSharedClassLoader;
import com.github.fridujo.junit.extension.classloader.UsingSharedClassLoaderInjection;

@UsingSharedClassLoaderInjection
class LolTest {

    @BeforeEach
    void setUp(@FromSharedClassLoader AtomicReference<Class<?>> stargate) throws ClassNotFoundException {
        stargate.set(Class.forName("org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider"));
    }

    @Test
    @ModifiedClasspath(excludeGavs = "junit-jupiter")
    void lol(@FromSharedClassLoader AtomicReference<Class<?>> stargate) {
        System.out.println(stargate.get());
    }

    @Test
    @ModifiedClasspath(replaceProductionCode = "com.github.fridujo:classpath-junit-extension:1.0.0")
    void lolilol() {
        System.out.println(PathElement.create("lol"));
        Assertions.assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> Class.forName("com.github.fridujo.junit.extension.classloader.UsingSharedClassLoaderInjection"));
    }
}
