import com.imindersingh.store.AbstractPostmanStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AbstractPostmanStoreTest {

    @Test
    void givenValidPathThenDirectoriesAreCreated(@TempDir Path tempDirectory) {
        final String root = tempDirectory.toAbsolutePath().toString();
        final Path path = Path.of(root + "/path/to/file");
        final AbstractPostmanStore abstractPostmanStore = mock(AbstractPostmanStore.class, Mockito.CALLS_REAL_METHODS);
        abstractPostmanStore.createDirectories(path.toString());
        assertTrue(Files.exists(path));
    }

}
