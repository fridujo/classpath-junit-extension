package com.github.fridujo.classpath.junit.extension;

import com.github.fridujo.classpath.junit.extension.jupiter.ModifiedClasspath;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class OtherExtensionsCompatibilityTests {

    private final String constructorInjectedParameter;

    private final AtomicInteger mutatedByExtensionBeforeTest = new AtomicInteger(-2);

    OtherExtensionsCompatibilityTests(@InjectStringWithRandomUUID String constructorInjectedParameter) {
        this.constructorInjectedParameter = constructorInjectedParameter;
    }

    @Test
    @ModifiedClasspath(addDependencies = "ch.qos.logback:logback-classic:1.2.3")
    void parameter_resolver_extensions_are_triggered(@InjectStringWithRandomUUID String methodInjectedParameter) throws ClassNotFoundException {
        assertThat(Class.forName("ch.qos.logback.core.Appender")).isInterface();
        assertThatNoException().isThrownBy(() -> UUID.fromString(constructorInjectedParameter));
        assertThatNoException().isThrownBy(() -> UUID.fromString(methodInjectedParameter));
    }

    @Test
    @ModifiedClasspath(addDependencies = "ch.qos.logback:logback-classic:1.2.3")
    @SetAtomicIntegerFieldTo(45)
    @Disabled("")
    void lifecycle_extensions_are_triggered() {
        assertThat(mutatedByExtensionBeforeTest).hasValue(45);
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtendWith(InjectStringWithRandomUUID.InjectStringWithRandomUUIDExtension.class)
    @interface InjectStringWithRandomUUID {
        class InjectStringWithRandomUUIDExtension implements ParameterResolver {

            @Override
            public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                boolean match = Arrays.stream(parameterContext.getParameter().getAnnotations())
                    .anyMatch(a -> {
                        boolean equals = a.annotationType().getName().equals(InjectStringWithRandomUUID.class.getName());
                        return equals;
                    });
                return match;
            }

            @Override
            public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                return UUID.randomUUID().toString();
            }
        }
    }

    /**
     * {@link ExecutableInvoker#invoke(Method, Object)}
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtendWith(SetAtomicIntegerFieldTo.SetAtomicIntegerToExtension.class)
    @interface SetAtomicIntegerFieldTo {

        int value();

        class SetAtomicIntegerToExtension implements BeforeEachCallback {

            @Override
            public void beforeEach(ExtensionContext context) {
                Method method = context.getTestMethod().get();
                Arrays.stream(method.getAnnotations())
                    .filter(a -> a.annotationType().getName().equals(SetAtomicIntegerFieldTo.class.getName()))
                    .findFirst()
                    .ifPresent(a -> {
                        Method valueMethod = ReflectionUtils.findMethod(a.getClass(), "value").get();
                        int value = (int) ReflectionUtils.invokeMethod(valueMethod, a);

                        Class<?> testClass = context.getTestClass().get();
                        Object testInstance = context.getTestInstance().get();
                        Arrays.stream(testClass.getDeclaredFields())
                            .filter(f -> f.getType() == AtomicInteger.class)
                            .map(ReflectionUtils::makeAccessible)
                            .map(f -> ReflectionUtils.tryToReadFieldValue(f, testInstance).toOptional())
                            .filter(v -> v.isPresent())
                            .map(Optional::get)
                            .map(AtomicInteger.class::cast)
                            .forEach(ai -> ai.set(value));
                    });
            }
        }
    }
}
