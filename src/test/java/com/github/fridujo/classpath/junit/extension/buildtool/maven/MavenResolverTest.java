package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import com.github.fridujo.classpath.junit.extension.utils.Reflections;
import com.github.fridujo.classpath.junit.extension.utils.SystemVariables;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MavenResolverTest {

    @Test
    void gather_maven_home_from_sys_props() {
        Optional<Path> mavenHome = new MavenResolver().getMavenHome();

        assertThat(
            Optional.ofNullable(new SystemVariables().get(MavenResolver.M2_HOME))
                .orElseGet(() -> new SystemVariables().get(MavenResolver.MAVEN_HOME))
        )
            .as(MavenResolver.M2_HOME + " or " + MavenResolver.MAVEN_HOME + " system property must be set on the build system (not required for users)")
            .isNotBlank();
        assertThat(mavenHome).isPresent();
        assertThat(mavenHome.get()).isDirectoryContaining(p -> Files.isDirectory(p) && p.getFileName().toString().equals("bin"));
    }

    @Test
    void lookup_default_local_repository() {
        assertThat(new SystemVariables().get(MavenResolver.USER_HOME)).isNotBlank();

        Optional<Path> localRepository = new MavenResolver().getLocalRepository();

        assertThat(localRepository).isPresent();
        assertThat(localRepository.get()).isDirectoryContaining(p -> Files.isDirectory(p) && p.getFileName().toString().equals("org"));
    }

    @Test
    void gather_maven_home_from_cli() {
        MavenResolver mavenResolver = new MavenResolver();
        SystemVariables variables = Mockito.mock(SystemVariables.class);
        Reflections.setFieldValue(mavenResolver, "systemVariables", variables);
        when(variables.get(any())).thenReturn(null);
        Optional<Path> mavenHome = mavenResolver.getMavenHome();

        assertThat(mavenHome).isPresent();
        assertThat(mavenHome.get()).isDirectoryContaining(p -> Files.isDirectory(p) && p.getFileName().toString().equals("bin"));
    }

    @Test
    void lookup_local_repository_from_cli() {
        MavenResolver mavenResolver = new MavenResolver();
        SystemVariables variables = Mockito.mock(SystemVariables.class);
        Reflections.setFieldValue(mavenResolver, "systemVariables", variables);
        when(variables.get(any())).thenReturn("not_existing");
        Optional<Path> localRepository = mavenResolver.getLocalRepository();

        assertThat(localRepository).isPresent();
        assertThat(localRepository.get()).isDirectoryContaining(p -> Files.isDirectory(p) && p.getFileName().toString().equals("org"));
    }
}
