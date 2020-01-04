package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import static java.util.Optional.empty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import com.github.fridujo.classpath.junit.extension.buildtool.BuildToolResolver;
import com.github.fridujo.classpath.junit.extension.utils.Processes;
import com.github.fridujo.classpath.junit.extension.utils.SystemVariables;

public class MavenResolver implements BuildToolResolver {
    static final String M2_HOME = "M2_HOME";
    static final String USER_HOME = "user.home";

    private static final Pattern MAVEN_HOME_LINE_PATTERN = Pattern.compile("^" + Pattern.quote("Maven home: ") + "(?<home>.+)$", Pattern.MULTILINE);
    private static final Pattern LOCAL_REPOSITORY_PATTERN = Pattern.compile(Pattern.quote("<localRepository>") + "(?<localRepo>.+)" + Pattern.quote("</localRepository>"));

    private final SystemVariables systemVariables = new SystemVariables();

    @Override
    public Optional<BuildTool> resolve(Set<PathElement> classpath) {
        Optional<Path> mavenHome = getMavenHome();
        Optional<Path> localRepository = getLocalRepository();

        if (mavenHome.isPresent() && localRepository.isPresent()) {
            return Optional.of(new Maven(mavenHome.get(), localRepository.get()));
        } else {
            return empty();
        }
    }

    Optional<Path> getMavenHome() {
        Optional<Path> mavenHomeFromSysProps = Optional.ofNullable(systemVariables.get(M2_HOME))
            .map(Paths::get)
            .filter(Files::exists)
            .filter(Files::isDirectory);
        if (mavenHomeFromSysProps.isPresent()) {
            return mavenHomeFromSysProps;
        } else {
            return getMavenHomeFromCli();
        }
    }

    Optional<Path> getLocalRepository() {
        Optional<Path> defaultLocalRepositoryPath = Optional.ofNullable(systemVariables.get(USER_HOME))
            .map(s -> Paths.get(s, ".m2", "repository"))
            .filter(Files::exists)
            .filter(Files::isDirectory);
        if (defaultLocalRepositoryPath.isPresent()) {
            return defaultLocalRepositoryPath;
        } else {
            return getLocalRepositoryFromCli();
        }
    }

    private Optional<Path> getMavenHomeFromCli() {
        Processes.ProcessResult result = Processes.launch("mvn -B -version");
        if (result.exitCode != 0) {
            return empty();
        }
        Matcher matcher = MAVEN_HOME_LINE_PATTERN.matcher(result.output);
        if (matcher.find()) {
            String homeStr = matcher.group("home");
            return Optional.of(Paths.get(homeStr).normalize());
        } else {
            return empty();
        }
    }

    private Optional<Path> getLocalRepositoryFromCli() {
        Processes.ProcessResult result = Processes.launch("mvn -B help:effective-settings");
        if (result.exitCode != 0) {
            return empty();
        }
        Matcher matcher = LOCAL_REPOSITORY_PATTERN.matcher(result.output);
        if (matcher.find()) {
            String homeStr = matcher.group("localRepo");
            return Optional.of(Paths.get(homeStr).normalize());
        } else {
            return empty();
        }
    }
}
