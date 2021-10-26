import com.imindersingh.model.Body;
import com.imindersingh.model.Header;
import com.imindersingh.model.Query;
import com.imindersingh.model.Request;
import com.imindersingh.model.Url;
import com.imindersingh.model.UrlEncoded;
import com.imindersingh.requests.PostmanRequestBuilder;
import kong.unirest.HttpRequest;
import kong.unirest.Unirest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PostmanRequestBuilderTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT = "text";
    private static final String CITY = "City";
    private static final HttpRequest<?> GET_REQUEST = Unirest
            .get("https://testapp-dev.com/id/1234585")
            .header(CONTENT_TYPE, TEXT)
            .header(CITY, "UK")
            .header("accept-encoding", "zip")
            .header("User-Agent", "id-gauge/1.0.0");
    private static final Request POSTMAN_REQUEST = Request.builder()
            .method("GET")
            .url(Url.builder()
                    .host(List.of("testapp-dev.com"))
                    .raw("https://testapp-dev.com/id/1234585")
                    .protocol("https")
                    .path(List.of("id", "1234585"))
                    .build())
            .body(Body.builder().raw("").mode("raw").build())
            .header(List.of(
                    Header.builder().key(CONTENT_TYPE).value(TEXT).build(),
                    Header.builder().key(CITY).value("UK").build(),
                    Header.builder().key("User-Agent").value("id-postman/1.0.0").build()))
            .description("Example GET request for endpoint: https://testapp-dev.com/id/1234585")
            .build();
    private static final HttpRequest<?> GET_REQUEST_WITH_PARAMS = Unirest
            .get("https://testapp-dev.com/hello/again?client=def123&device=abc")
            .header(CONTENT_TYPE, TEXT)
            .header(CITY, "UK");
    private static final HttpRequest<?> GET_REQUEST_WITH_EMPTY_PARAMS = Unirest
            .get("https://testapp-dev.com/hello/again?client=")
            .header(CONTENT_TYPE, TEXT);
    private static final HttpRequest<?> POST_REQUEST_WITH_UNI_BODY = Unirest
            .post("https://testapp-dev.com/hello/again?param=def&paramtwo=abc")
            .header(CONTENT_TYPE, TEXT)
            .header(CITY, "UK")
            .body("{\"profileId\": \"12sas-28392\"}");
    private static final HttpRequest<?> POST_REQUEST_WITH_MULTI_BODY = Unirest
            .post("https://testapp-dev.com/hello/again?param=def&paramtwo=abc")
            .header(CONTENT_TYPE, TEXT)
            .header(CITY, "UK")
            .fields(new HashMap<>() {
                {
                    put("field1", "fieldValue123");
                    put("field2", "fieldValue134!");
                }
            });

    private final PostmanRequestBuilder postmanRequestBuilder = new PostmanRequestBuilder();

    @Test
    void givenHttpRequestThenCanBuildPostmanRequest() {
        final Request request = postmanRequestBuilder.requestBuilder(GET_REQUEST);
        assertNotNull(request);
        assertEquals(POSTMAN_REQUEST.getUrl(), request.getUrl());
        assertEquals(POSTMAN_REQUEST.getHeader(), request.getHeader());
        assertEquals(POSTMAN_REQUEST.getBody(), request.getBody());
        assertEquals(POSTMAN_REQUEST.getDescription(), request.getDescription());
    }

    @Test
    void givenUrlHasClientAndDeviceQueryParamsThenCanBuildPostmanRequest() {
        final Request request = postmanRequestBuilder.requestBuilder(GET_REQUEST_WITH_PARAMS);
        assertNotNull(request);
        List<Query> expectedQueryList = new ArrayList<>() {
            {
                add(Query.builder().key("client").value("def123").build());
                add(Query.builder().key("device").value("abc").build());
            }
        };
        assertEquals(expectedQueryList, request.getUrl().getQuery());
    }

    @Test
    void givenUrlHasEmptyQueryParamsThenCanBuildPostmanRequest() {
        final Request request = postmanRequestBuilder.requestBuilder(GET_REQUEST_WITH_EMPTY_PARAMS);
        assertNotNull(request);
        List<Query> expectedQueryList = new ArrayList<>() {
            {
                add(Query.builder().key("client").value("").build());
            }
        };
        assertEquals(expectedQueryList, request.getUrl().getQuery());
    }

    @Test
    void givenRequestWithUniBodyThenCanBuildPostmanRequest() {
        final Request request = postmanRequestBuilder.requestBuilder(POST_REQUEST_WITH_UNI_BODY);
        assertNotNull(request);
        Body expectedBody = Body.builder().mode("raw").raw("{\"profileId\": \"12sas-28392\"}").build();
        assertEquals(expectedBody, request.getBody());
    }

    @Test
    void givenRequestWithMultiPartBodyThenCanBuildPostmanRequest() {
        final Request request = postmanRequestBuilder.requestBuilder(POST_REQUEST_WITH_MULTI_BODY);
        assertNotNull(request);
        Body expectedBody = Body.builder()
                .mode("urlencoded")
                .raw("")
                .urlEncoded(List.of(
                        UrlEncoded.builder().key("field1").value("fieldValue123").build(),
                        UrlEncoded.builder().key("field2").value("fieldValue134!").build()
                ))
                .build();
        assertEquals(expectedBody, request.getBody());
    }
}
