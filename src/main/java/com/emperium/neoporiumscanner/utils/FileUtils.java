package com.emperium.neoporiumscanner.utils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static boolean ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    public static boolean ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    public static String readFileToString(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }

    public static void writeStringToFile(String filePath, String content) throws IOException {
        Files.writeString(Paths.get(filePath), content);
    }

    public static void appendToFile(String filePath, String content) throws IOException {
        Files.writeString(Paths.get(filePath), content,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static List<String> readLines(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    public static void writeLines(String filePath, List<String> lines) throws IOException {
        Files.write(Paths.get(filePath), lines);
    }

    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteDirectory(String dirPath) {
        return deleteDirectory(new File(dirPath));
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        return dir.delete();
    }

    public static List<File> listFiles(String dirPath, String extension) {
        List<File> files = new ArrayList<>();
        File dir = new File(dirPath);

        if (dir.exists() && dir.isDirectory()) {
            File[] fileArray = dir.listFiles((d, name) ->
                    name.toLowerCase().endsWith(extension.toLowerCase()));

            if (fileArray != null) {
                files.addAll(Arrays.asList(fileArray));
            }
        }

        return files;
    }

    public static List<File> listFilesSortedByDate(String dirPath, String extension) {
        List<File> files = listFiles(dirPath, extension);
        files.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return files;
    }

    public static String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static String generateTimestampFileName(String prefix, String extension) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return prefix + "_" + timestamp + "." + extension;
    }

    public static String getFileSizeString(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static boolean copyFile(String sourcePath, String destPath) {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(destPath),
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean moveFile(String sourcePath, String destPath) {
        try {
            Files.move(Paths.get(sourcePath), Paths.get(destPath),
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void zipDirectory(String sourceDir, String zipFilePath) throws IOException {
        Path sourcePath = Paths.get(sourceDir);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Error adding file to zip: " + e.getMessage());
                        }
                    });
        }
    }

    public static String readResourceFile(String resourcePath) {
        try (InputStream is = FileUtils.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return new String(is.readAllBytes());
            }
        } catch (IOException e) {
            System.err.println("Error reading resource file: " + e.getMessage());
        }
        return "";
    }

    public static Properties loadProperties(String filePath) {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filePath)) {
            props.load(is);
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
        return props;
    }

    public static void saveProperties(Properties props, String filePath) {
        try (OutputStream os = new FileOutputStream(filePath)) {
            props.store(os, "Neoporium Scanner Configuration");
        } catch (IOException e) {
            System.err.println("Error saving properties: " + e.getMessage());
        }
    }

    public static String getRelativePath(File base, File file) {
        return base.toPath().relativize(file.toPath()).toString();
    }

    public static File createTempFile(String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix);
    }

    public static void cleanOldFiles(String directory, String extension, int keepLastDays) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        long cutoff = System.currentTimeMillis() - (keepLastDays * 24L * 60 * 60 * 1000);
        File[] files = dir.listFiles((d, name) -> name.endsWith(extension));

        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < cutoff) {
                    file.delete();
                }
            }
        }
    }
}