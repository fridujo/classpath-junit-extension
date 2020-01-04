package com.github.fridujo.classpath.junit.extension.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.condition.OS;

public class Processes {

    public static ProcessResult launch(String command) {
        ProcessBuilder builder = new ProcessBuilder();
        if (OS.WINDOWS.isCurrentOs()) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        try {
            Process process = builder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ProcessResult.error(exitCode);
            }
            String output = new Scanner(process.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
            return ProcessResult.success(output);
        } catch (IOException | InterruptedException e) {
            return ProcessResult.error(e);
        }
    }

    public static final class ProcessResult {

        public final Exception cause;
        public final int exitCode;
        public final String output;

        public ProcessResult(Exception cause, int exitCode, String output) {
            this.cause = cause;
            this.exitCode = exitCode;
            this.output = output;
        }

        public static ProcessResult error(Exception cause) {
            return new ProcessResult(cause, -1, null);
        }

        public static ProcessResult error(int exitCode) {
            return new ProcessResult(null, exitCode, null);
        }

        public static ProcessResult success(String output) {
            return new ProcessResult(null, 0, output);
        }
    }
}
