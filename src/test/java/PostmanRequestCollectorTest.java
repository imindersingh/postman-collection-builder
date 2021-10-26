import com.imindersingh.requests.PostmanRequestCollector;
import com.imindersingh.requests.PostmanRequestStore;
import kong.unirest.Config;
import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestSummary;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostmanRequestCollectorTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT = "text";
    private static final String CITY = "City";
    private static final Config CONFIG = Unirest.config();
    private static final HttpRequest<?> GET_HTTP_REQUEST = Unirest
            .get("https://testapp-dev.com/id/1234585")
            .header(CONTENT_TYPE, TEXT)
            .header(CITY, "LEEDS")
            .header("accept-encoding", "zip")
            .header("User-Agent", "id-gauge/1.0.0");
    private static final String POSTMAN_REQUEST = "{\"body\":{\"mode\":\"raw\",\"raw\":\"\",\"urlencoded\":null},"
            + "\"description\":\"Example GET request for endpoint: https://testapp-dev.com/id/1234585\","
            + "\"header\":[{\"key\":\"Content-Type\",\"value\":\"text\"},"
            + "{\"key\":\"City\",\"value\":\"LEEDS\"},"
            + "{\"key\":\"User-Agent\",\"value\":\"id-postman/1.0.0\"}],"
            + "\"method\":\"GET\","
            + "\"url\":{\"host\":[\"testapp-dev.com\"],"
            + "\"path\":[\"id\",\"1234585\"],"
            + "\"raw\":\"https://testapp-dev.com/id/1234585\","
            + "\"protocol\":\"https\","
            + "\"query\":null,"
            + "\"variable\":null}}";

    @InjectMocks
    private PostmanRequestCollector postmanRequestCollector;

    @Mock
    private HttpResponse<JsonNode> response;

    @Mock
    private HttpRequestSummary requestSummary;

    @Mock
    private PostmanRequestStore postmanRequestStore;

    @AfterEach
    void tearDown() {
        postmanRequestCollector.setHttpRequest(null);
    }

    @Test
    void givenResponseStatusStartsWithTwoThenRequestIsCollected() {
        when(response.getStatus()).thenReturn(200);
        postmanRequestCollector.setHttpRequest(GET_HTTP_REQUEST);
        postmanRequestCollector.onResponse(response, requestSummary, CONFIG);

        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
        verify(postmanRequestStore).save(valueCapture.capture());
        verify(postmanRequestStore, times(1)).save(Mockito.anyString());
        assertEquals(POSTMAN_REQUEST, valueCapture.getValue());
    }

    @Test
    void givenResponseStatusStartsWithThreeThenRequestIsCollected() {
        when(response.getStatus()).thenReturn(300);
        postmanRequestCollector.setHttpRequest(GET_HTTP_REQUEST);
        postmanRequestCollector.onResponse(response, requestSummary, CONFIG);

        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
        verify(postmanRequestStore).save(valueCapture.capture());
        verify(postmanRequestStore, times(1)).save(Mockito.anyString());
        assertEquals(POSTMAN_REQUEST, valueCapture.getValue());
    }

    @Test
    void givenResponseStatusDoesNotStartWithTwoOrThreeThenRequestIsNotCollected() {
        when(response.getStatus()).thenReturn(400);
        postmanRequestCollector.setHttpRequest(GET_HTTP_REQUEST);
        postmanRequestCollector.onResponse(response, requestSummary, CONFIG);
        verify(postmanRequestStore, times(0)).save(Mockito.anyString());
    }
}