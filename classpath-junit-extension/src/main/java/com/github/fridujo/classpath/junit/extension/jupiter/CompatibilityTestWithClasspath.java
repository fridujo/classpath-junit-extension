package com.github.fridujo.classpath.junit.extension.jupiter;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * {@code @CompatibilityTestWithClasspath} is used to signal that the annotated method is a
 * <em>test template</em> method that should be repeated with different classpaths.
 *
 * <p>Classpaths are computed as a matrix composed of every combination of versions of the given dependencies.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@TestTemplate
@ExtendWith(CompatibleWithDependenciesExtension.class)
public @interface CompatibilityTestWithClasspath {

    /**
     * A set of <b>dependency ranges</b>.
     *
     * <p>A <b>dependency range</b> can have the following structures:
     * <ul>
     * <li>artifactId[version]</li>
     * <li>artifactId[version1, version2, etc.]</li>
     * <li>groupId:artifactId[version]</li>
     * <li>groupId:artifactId[version1, version2, etc.]</li>
     * </ul>
     */
    String[] dependencies();

    /**
     * Include the current classpath as the first line of the compatibility matrix.
     *
     * <p>Default is {@code true}.
     */
    boolean withCurrent() default true;
}
