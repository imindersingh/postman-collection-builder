package com.imindersingh.requests;

import com.imindersingh.store.AbstractPostmanStore;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class PostmanRequestStore extends AbstractPostmanStore {

    private final String storeName;
    private final String storePath;

    public PostmanRequestStore(String storeName, String storePath) {
        this.storeName = storeName;
        this.storePath = storePath;
    }

    @Override
    public void save(final String request) {
        if (request.isEmpty()) {
            throw new RuntimeException("Postman request is empty");
        }
        createDirectories(storePath);
        final File file = new File(storeName);
        try (PrintWriter printWriter = new PrintWriter(Files.newOutputStream(Paths.get(storePath + "/" + file), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            printWriter.println(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to add request to postman store", e);
        }
    }

    public List<String> getRequestsFromStore() {
        List<String> result;
        try {
            result = Files.readAllLines(Paths.get(storePath + "/" + storeName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from postman store", e);
        }
        return result;
    }
}
