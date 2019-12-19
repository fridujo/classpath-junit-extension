package com.github.fridujo.junit.extension.classpath.maven;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.condition.OS;

public class MavenHome {

    private static final Pattern MAVEN_HOME_LINE_PATTERN = Pattern.compile("^" + Pattern.quote("Maven home: ") + "(?<home>.+)$", Pattern.MULTILINE);

    public static void fromCli() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        if (OS.WINDOWS.isCurrentOs()) {
            // mvn help:effective-settings for localRepository
            builder.command("cmd.exe", "/c", "mvn -version");
        } else {
            builder.command("sh", "-c", "mvn -version");
        }

        Process process = builder.start();

        int exitCode = process.waitFor();
        assert exitCode == 0;
        String output = new Scanner(process.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
        Matcher matcher = MAVEN_HOME_LINE_PATTERN.matcher(output);
        if(matcher.find()) {
            String homeStr = matcher.group("home");
            System.out.println(Paths.get(homeStr).normalize());
        }
    }


}
