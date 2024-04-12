package com.github.fridujo.classpath.junit.extension.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Modify the <b>current</b> classpath before running the marked test.<br>
 * <br>
 * <p style="color: red;"><b>
 * Most of the parameters are available if the project uses one of the supported {@link com.github.fridujo.classpath.junit.extension.buildtool Build Tools}.
 * </b>, however {@link #excludeJars()} can be used in any case.</p><br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@ExtendWith(ModifiedClasspathExtension.class)
public @interface ModifiedClasspath {

    /**
     * Will remove Jars matching the given GAVs (groupId:artifactId:version).<br>
     * <p>
     * GAVs can have the following structures:
     * <ul>
     * <li><b>artifactId</b>: aka JAR name</li>
     * <li><b>groupId:artifactId</b></li>
     * <li><b>groupId:artifactId:version</b></li>
     * </ul>
     * <p>
     * In opposition to {@link #excludeDependencies()}, their will be no attempt to list and exclude dependencies.
     */
    String[] excludeJars() default {};

    /**
     * Will remove Dependencies matching the given GAVs (groupId:artifactId:version).<br>
     * <p>
     * GAVs can have the following structures:
     * <ul>
     * <li>artifactId : aka JAR name</li>
     * <li>groupId:artifactId</li>
     * <li>groupId:artifactId:version</li>
     * </ul>
     * <p>
     * Their dependencies (transitive or not) will also be excluded.
     */
    String[] excludeDependencies() default {};

    /**
     * Will add Dependencies matching the given <u>Absolute</u> GAV (groupId:artifactId:version).<br>
     * <p>
     * <u>Absolute</u> Gav must have the following structures:
     * <ul>
     * <li>groupId:artifactId:version</li>
     * </ul>
     * <p>
     * Their dependencies (transitive or not) will also be added.
     */
    String[] addDependencies() default {};
}
