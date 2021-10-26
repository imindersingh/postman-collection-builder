package com.imindersingh.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractPostmanStore {

    public abstract void save(String data);

    public static String getDate() {
        final LocalDateTime date = LocalDateTime.now();
        final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(format);
    }

    public void createDirectories(final String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories", e);
        }
    }

}
