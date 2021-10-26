package com.imindersingh.requests;

import com.imindersingh.helpers.SerializationHelper;
import kong.unirest.Config;
import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestSummary;
import kong.unirest.HttpResponse;
import kong.unirest.Interceptor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class PostmanRequestCollector implements Interceptor {

    private final PostmanRequestStore postmanRequestStoreStore;

    private List<HttpRequest<?>> httpRequestList = new ArrayList<>();
    @Setter private HttpRequest<?> httpRequest;

    public PostmanRequestCollector(PostmanRequestStore postmanRequestStoreStore) {
        this.postmanRequestStoreStore = postmanRequestStoreStore;
    }

    @Override
    public void onRequest(HttpRequest<?> request, Config config) {
        this.httpRequest = request;
        //httpRequestList.add(request);
    }

    @Override
    public void onResponse(HttpResponse<?> response, HttpRequestSummary request, Config config) {

        if (response.getParsingError().isEmpty()
                && response.getStatus() >= 200
                && response.getStatus() < 400) {

            final String postmanRequest = SerializationHelper.asString(new PostmanRequestBuilder().requestBuilder(httpRequest));
            postmanRequestStoreStore.save(postmanRequest);
            httpRequestList.clear();
        }
    }

}
