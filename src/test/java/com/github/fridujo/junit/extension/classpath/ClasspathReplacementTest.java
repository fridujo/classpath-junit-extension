package com.github.fridujo.junit.extension.classpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class ClasspathReplacementTest {

    @Test
    @ModifiedClasspath(replaceGavs = {
        @ReplaceGav(original = "assertj-core", replacement = "org.assertj:assertj-core:2.5.0")
    })
    void replacement_of_dependency_by_another() {
        try {
            assertThat("857423").containsOnlyDigits();
            fail("The method assertThat(String) should not exists in version 2.5.0");
        } catch(NoSuchMethodError e) {
            System.out.println("For information:");
            e.printStackTrace(System.out);
        }
    }
}
