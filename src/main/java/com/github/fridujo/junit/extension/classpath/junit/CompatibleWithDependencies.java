package com.github.fridujo.junit.extension.classpath.junit;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@TestTemplate
@ExtendWith(ClasspathMatrixExtension.class)
public @interface CompatibleWithDependencies {

    String[] value();

    boolean current() default false;
}
