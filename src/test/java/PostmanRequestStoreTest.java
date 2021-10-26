import com.imindersingh.requests.PostmanRequestStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostmanRequestStoreTest {

    private static final String STORE = "postman.db";
    private static final String REQUEST = "{\"body\":{\"mode\":\"raw\",\"raw\":\"\",\"urlencoded\":null},"
            + "\"description\":\"Example GET request for endpoint: https://testapp-dev.com/id/1234585\","
            + "\"header\":[{\"key\":\"Content-Type\",\"value\":\"text\"},"
            + "{\"key\":\"Country\",\"value\":\"UK\"},"
            + "{\"key\":\"User-Agent\",\"value\":\"id-postman/1.0.0\"}],"
            + "\"method\":\"GET\","
            + "\"url\":{\"host\":[\"testapp-dev.com\"],"
            + "\"path\":[\"id\",\"1234585\"],"
            + "\"raw\":\"https://testapp-dev.com/id/1234585\","
            + "\"protocol\":\"https\","
            + "\"query\":null,"
            + "\"variable\":null}}";
    private final Path file = tempDirectory.resolve(STORE);
    private final String storePath = tempDirectory.toAbsolutePath().toString();
    private final String fileFullPath = file.toAbsolutePath().toString();
    private final PostmanRequestStore postmanRequestStore = new PostmanRequestStore(STORE, storePath);
    private final List<String> requests = new ArrayList<>(){
        {
            add(REQUEST);
            add(REQUEST);
        }
    };

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
    void givenRequestsThenCanWriteToStore() throws IOException {
        postmanRequestStore.save(requests.get(0));
        postmanRequestStore.save(requests.get(1));
        final List<String> expectedRequests = Files.readAllLines(Paths.get(fileFullPath));
        assertEquals(expectedRequests, requests);
    }

    @Test
    void givenRequestsAreStoredThenCanRetrieve() {
        postmanRequestStore.save(requests.get(0));
        postmanRequestStore.save(requests.get(1));
        final List<String> expectedRequests = postmanRequestStore.getRequestsFromStore();
        assertEquals(expectedRequests, requests);
    }

    @Test
    void givenInvalidPathThenExceptionIsThrown() {
        final PostmanRequestStore postmanRequestStore = new PostmanRequestStore(STORE, "invalid");
        final Exception exception = assertThrows(RuntimeException.class, postmanRequestStore::getRequestsFromStore);
        assertThat("Failed to read from postman store", is(exception.getMessage()));
    }
}
