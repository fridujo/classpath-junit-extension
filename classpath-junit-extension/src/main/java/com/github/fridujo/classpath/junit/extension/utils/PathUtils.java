package com.github.fridujo.classpath.junit.extension.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class PathUtils {

    public static void delete(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult postVisitDirectory(
                        Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                        Path file, BasicFileAttributes attrs)
                        throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete: " + path, e);
        }
    }
}
