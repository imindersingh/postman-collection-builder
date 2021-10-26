package com.imindersingh.collection;

import com.imindersingh.store.AbstractPostmanStore;
import org.assertj.core.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class PostmanCollectionStore extends AbstractPostmanStore {

    private final String collectionName;
    private final String collectionPath;

    public PostmanCollectionStore(String collectionName, String collectionPath) {
        this.collectionName = collectionName;
        this.collectionPath = collectionPath;
    }

    @Override
    public void save(String collection) {
        if (Strings.isNullOrEmpty(collection)) {
            throw new RuntimeException("Postman collection is not valid");
        }
        createDirectories(collectionPath);
        final File file = new File(collectionName + ".json");
        try (PrintWriter printWriter = new PrintWriter(Files.newOutputStream(Paths.get(collectionPath + "/" + file), StandardOpenOption.CREATE))) {
            printWriter.print(collection);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save postman collection to file", e);
        }
    }

}
