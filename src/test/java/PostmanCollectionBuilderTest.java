import com.imindersingh.helpers.SerializationHelper;
import com.imindersingh.collection.PostmanCollectionBuilder;
import com.imindersingh.model.Collection;
import com.imindersingh.model.Variable;
import com.imindersingh.requests.PostmanRequestBuilder;
import kong.unirest.HttpRequest;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PostmanCollectionBuilderTest {

    private static final String COUNTRY = "Country";
    private static final String CITY = "City";
    private static final String ENVIRONMENT = "int";
    private static final String APPLICATION = "testapp";
    private static final String HOST = "testapp-dev.com";
    private static final String UK = "UK";
    private static final String LEEDS = "LEEDS";

    private static final HttpRequest<?> GET_REQUEST = Unirest
            .get("https://testapp-dev.com/id/1234585")
            .header(COUNTRY, UK)
            .header(CITY, LEEDS);
    private static final HttpRequest<?> POST_REQUEST = Unirest
            .post("https://testapp-dev.com/id/test@test.com")
            .header(COUNTRY, UK)
            .header(CITY, LEEDS);
    private static final HttpRequest<?> SENSITIVE_REQUEST_UNI_BODY = Unirest
            .post("https://testapp-dev.com/id/test@test.com")
            .header("Authorization", "Basic 123xx")
            .body("{\"password\":\"test1345\",\"confirmPassword\":\"test1345\",\"userIdentifier\":\"test1345@test.com\"}");
    private static final HttpRequest<?> SENSITIVE_REQUEST_MULTI_BODY = Unirest
            .post("https://testapp-dev.com/hello/again?param=def&paramtwo=abc")
            .header("Authorization", "Basic 123xx")
            .fields(new HashMap<>() {
                {
                    put("password", "test1234");
                    put("confirmPassword", "test1234");
                    put("userIdentifier", "test@test.com");
                }
            });
    private final PostmanCollectionBuilder postmanCollectionBuilder = new PostmanCollectionBuilder(
            ENVIRONMENT,
            APPLICATION,
            HOST);

    private static final List<HttpRequest<?>> REQUESTS = new ArrayList<>();

    @AfterEach
    void tearDown() {
        REQUESTS.clear();
    }

    @Test
    void givenDuplicateRequestsThenCollectionContainsSingleCityAndCountryFolderWithUniqueRequest() {
        REQUESTS.add(GET_REQUEST);
        REQUESTS.add(GET_REQUEST);

        Collection collection = createCollection();
        assertEquals(1, collection.getItem().size());
        assertEquals(LEEDS, collection.getItem().get(0).getName());
        assertEquals(1, collection.getItem().get(0).getItems().size());
        assertEquals(UK, collection.getItem().get(0).getItems().get(0).getName());
        assertNotNull(collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest());
    }

    @Test
    void givenCountryAndCityHeaderNotPresentThenCollectionFoldersAreNoCityAndNoCountry() {
        final HttpRequest<?> getRequest = Unirest
                .get("https://testapp-dev.com/id/1234-585");
        REQUESTS.add(getRequest);
        REQUESTS.add(getRequest);

        Collection collection = createCollection();
        assertEquals(1, collection.getItem().size(), 1);
        assertEquals("NO_CITY", collection.getItem().get(0).getName());
        assertEquals(1, collection.getItem().get(0).getItems().size());
        assertEquals("NO_COUNTRY", collection.getItem().get(0).getItems().get(0).getName());
    }

    @Test
    void givenHostThenHostSpecificRequestsAreCaptured() {
        final HttpRequest<?> getTestRequest = Unirest
                .get("https://dummyapp-dev.com/id/1234585")
                .header(COUNTRY, UK)
                .header(CITY, LEEDS);
        REQUESTS.add(GET_REQUEST);
        REQUESTS.add(getTestRequest);

        Collection collection = createCollection();
        assertEquals(1, collection.getItem().get(0).getItems().get(0).getItems().size());
        assertEquals(HOST,
                collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest().getUrl().getHost().get(0));
    }

    @Test
    void givenRequestsWithSamePathButDifferentHttpMethodThenBothAreReturned() {
        REQUESTS.add(GET_REQUEST);
        REQUESTS.add(POST_REQUEST);
        Collection collection = createCollection();
        assertEquals(2, collection.getItem().get(0).getItems().get(0).getItems().size());
    }

    @Test
    void givenRequestsWithSamePathAndHttpMethodButDifferentCityCountryThenBothAreReturned() {
        final HttpRequest<?> getTestAppRequest = Unirest
                .get("https://testapp-dev.com/id/1234-585-87878")
                .header(COUNTRY, "IE")
                .header(CITY, "LONDON");

        REQUESTS.add(GET_REQUEST);
        REQUESTS.add(getTestAppRequest);

        Collection collection = createCollection();
        assertEquals(2, collection.getItem().size(), 2);
        assertEquals(LEEDS, collection.getItem().get(0).getName());
        assertEquals(1, collection.getItem().get(0).getItems().size());
        assertEquals(UK, collection.getItem().get(0).getItems().get(0).getName());
        assertEquals("LONDON", collection.getItem().get(1).getName());
        assertEquals(1, collection.getItem().get(1).getItems().size());
        assertEquals("IE", collection.getItem().get(1).getItems().get(0).getName());
    }

    @Test
    void whenRequestContainsPathParametersThenParametersAreAddedToVariableList() {
        final HttpRequest<?> getRequest = Unirest
                .get("https://testapp-dev.com/id/test@test.com/service/3092-3092")
                .header(COUNTRY, UK)
                .header(CITY, LEEDS);
        REQUESTS.add(getRequest);

        Collection collection = createCollection();

        assertNotNull(collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest());
        assertEquals("/id/:param/service/:param",
                collection.getItem().get(0).getItems().get(0).getItems().get(0).getName());
        assertEquals("https://testapp-dev.com/id/:param/service/:param",
                collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest().getUrl().getRaw());
        final List<Variable> variables = new ArrayList<>() {{
            add(Variable.builder().key(":param").value("test@test.com").build());
            add(Variable.builder().key(":param").value("3092-3092").build());
        }};
        assertEquals(variables,
                collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest().getUrl().getVariable());
    }

    @Test
    void whenRequestContainsQueryParametersThenQueryParametersAreAddedToVariableList() {
        final HttpRequest<?> getRequest = Unirest
                .get("https://testapp-dev.com/id/test@test.com/service?a=1&b=2&c=3")
                .header(COUNTRY, UK)
                .header(CITY, LEEDS);
        REQUESTS.add(getRequest);

        Collection collection = createCollection();

        assertNotNull(collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest());
        assertEquals("/id/:param/service",
                collection.getItem().get(0).getItems().get(0).getItems().get(0).getName());
        assertEquals("https://testapp-dev.com/id/:param/service?a=1&b=2&c=3",
                collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest().getUrl().getRaw());
        final List<Variable> expectedVariableList = new ArrayList<>() {{
            add(Variable.builder().key(":param").value("test@test.com").build());
            add(Variable.builder().key("a").value("1").build());
            add(Variable.builder().key("b").value("2").build());
            add(Variable.builder().key("c").value("3").build());
        }};
        assertEquals(expectedVariableList,
                collection.getItem().get(0).getItems().get(0).getItems().get(0).getRequest().getUrl().getVariable());
    }

    @Test
    void givenPostmanCollectionThenInfoIsCorrect() {
        REQUESTS.add(GET_REQUEST);

        Collection collection = createCollection();
        final String uuidPattern = "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})";
        assertTrue(collection.getInfo().getPostmanId().matches(uuidPattern));
        final LocalDateTime date = LocalDateTime.now();
        final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        assertEquals(String.format("%s-%s - v%s", APPLICATION, ENVIRONMENT, date.format(format)),
                collection.getInfo().getName());
    }

//    @Test
//    void givenCollectionWithUniBodyRequestContainsSensitiveDataThenCanRedact() {
//        REQUESTS.add(SENSITIVE_REQUEST_UNI_BODY);
//        Collection collection = createCollection();
//        final String expectedHeaderRedaction =
//                "[{\"key\":\"Authorization\",\"value\":\"[REDACTED]\"}]";
//        final String redactedCollection = postmanCollectionBuilder.getCollectionJsonStr(collection);
//        assertTrue(redactedCollection.contains(expectedHeaderRedaction));
//        final String expectedUniBodyRedaction = "\"{\\\"password\\\":\\\"[REDACTED]\\\","
//                + "\\\"confirmPassword\\\":\\\"[REDACTED]\\\","
//                + "\\\"userIdentifier\\\":\\\"[REDACTED]\\\"}\"";
//        assertTrue(redactedCollection.contains(expectedUniBodyRedaction));
//    }

//    @Test
//    void givenCollectionWithMultiBodyRequestContainsSensitiveDataThenCanRedact() {
//        REQUESTS.add(SENSITIVE_REQUEST_MULTI_BODY);
//        final String redactedCollection = postmanCollectionBuilder.getCollectionJsonStr(createCollection());
//        final String expectedHeaderRedaction =
//                "[{\"key\":\"Authorization\",\"value\":\"[REDACTED]\"}]";
//        assertTrue(redactedCollection.contains(expectedHeaderRedaction));
//        final String expectedUniBodyRedaction = "[{\"key\":\"confirmPassword\",\"value\":\"[REDACTED]\"},"
//                + "{\"key\":\"password\",\"value\":\"[REDACTED]\"},"
//                + "{\"key\":\"userIdentifier\",\"value\":\"[REDACTED]\"}]";
//        assertTrue(redactedCollection.contains(expectedUniBodyRedaction));
//    }

    private Collection createCollection() {
        final List<String> requestList = new ArrayList<>();
        for (HttpRequest<?> httpRequest : REQUESTS) {
            requestList.add(SerializationHelper.asString(new PostmanRequestBuilder().requestBuilder(httpRequest)));
        }
        return postmanCollectionBuilder.createPostmanCollection(requestList);
    }
}