package com.github.fridujo.junit.extension.classpath;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Modify the <b>current</b> classpath before running the marked test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@ExtendWith(ModifiedClasspathExtension.class)
public @interface ModifiedClasspath {
    /**
     * Will remove jars matching the given GAV (groupId:artifactId:version).<br>
     * <p>
     * Gav can have the following structures:
     * <ul>
     * <li>artifactId : aka JAR name</li>
     * <li>groupId:artifactId</li>
     * <li>groupId:artifactId:version</li>
     * </ul>
     */
    String[] excludeJars() default {};
}
