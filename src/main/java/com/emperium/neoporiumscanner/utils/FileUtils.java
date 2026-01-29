package com.emperium.neoporiumscanner.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static void ensureDirectory(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static String readFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    public static void writeFile(File file, String content) throws IOException {
        ensureDirectory(file.getParentFile());
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}