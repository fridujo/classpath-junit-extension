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
     * Will remove Jars matching the given GAV (groupId:artifactId:version).<br>
     * <p>
     * Gav can have the following structures:
     * <ul>
     * <li>artifactId : aka JAR name</li>
     * <li>groupId:artifactId</li>
     * <li>groupId:artifactId:version</li>
     * </ul>
     * <p>
     * If the matching Jars are in a <b>Maven</b> repository, their dependencies (transitive or not) will also be excluded.
     */
    String[] excludeGavs() default {};

    /**
     * Will remove Jars matching the given GAV (groupId:artifactId:version).<br>
     * <p>
     * Gav can have the following structures:
     * <ul>
     * <li><b>artifactId</b>: aka JAR name</li>
     * <li><b>groupId:artifactId</b></li>
     * <li><b>groupId:artifactId:version</b></li>
     * </ul>
     * <p>
     * <p>
     * In opposition to {@link #excludeGavs()}, their will be no attempt to list and exclude dependencies.
     */
    String[] excludeJars() default {};

}
