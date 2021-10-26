import com.imindersingh.collection.PostmanCollectionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostmanCollectionStoreTest {

    private static final String COLLECTION_NAME = "collection";
    private final String collectionPath = tempDirectory.toAbsolutePath().toString();
    private final String fileFullPath = String.format("%s/%s.json", collectionPath, COLLECTION_NAME);
    private final PostmanCollectionStore postmanCollectionStore = new PostmanCollectionStore(COLLECTION_NAME, collectionPath);

    @TempDir
    protected static Path tempDirectory;

    @AfterEach
    void tearDown() {
        if (Files.exists(Paths.get(fileFullPath))) {
            try {
                Files.delete(Paths.get(fileFullPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void givenCollectionStringThenCanSave() throws IOException {
        final String collection = "collecton";
        postmanCollectionStore.save(collection);
        final String savedCollection = Files.readAllLines(Paths.get(fileFullPath)).get(0);
        final int collectionSize = Files.readAllLines(Paths.get(fileFullPath)).size();
        assertEquals(1, collectionSize);
        assertEquals(collection, savedCollection);
    }

    @Test
    void givenNewCollectionThenCanOverwritePreviousCollection() throws IOException {
        final String collection = "collecton";
        postmanCollectionStore.save(collection);
        final String newCollection = "new collection";
        postmanCollectionStore.save(newCollection);
        final String savedCollection = Files.readAllLines(Paths.get(fileFullPath)).get(0);
        final int collectionSize = Files.readAllLines(Paths.get(fileFullPath)).size();
        assertEquals(1, collectionSize);
        assertEquals(newCollection, savedCollection);
    }

    @Test
    void givenInvalidCollectionThenExceptionIsThrown() {
        final Exception exception = assertThrows(RuntimeException.class, () -> {
            postmanCollectionStore.save("");
        });
        assertThat("Postman collection is not valid", is(exception.getMessage()));
    }

}
