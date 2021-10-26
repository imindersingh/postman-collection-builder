package com.imindersingh.requests;

import com.imindersingh.model.Body;
import com.imindersingh.model.Header;
import com.imindersingh.model.Query;
import com.imindersingh.model.Request;
import com.imindersingh.model.Url;
import com.imindersingh.model.UrlEncoded;
import kong.unirest.HttpRequest;
import kong.unirest.ParamPart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostmanRequestBuilder {

    private static final String ACCEPT_ENCODING = "accept-encoding";
    private static final String USER_AGENT = "User-Agent";

    public Request requestBuilder(final HttpRequest<?> request) {
        final URL url = url(request.getUrl());

        final Url postmanUrl = postmanUrlBuilder(url);
        final List<Header> postmanHeaders = postmanHeadersBuilder(request);
        final Body postmanBody = getBody(request);


        return Request.builder()
                .method(request.getHttpMethod().name())
                .url(postmanUrl)
                .header(postmanHeaders)
                .body(postmanBody)
                .description(String.format(
                        "Example %s request for endpoint: %s",
                        request.getHttpMethod().name(),
                        request.getUrl()))
                .build();
    }

    private URL url(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error with url: " + urlStr, e);
        }
    }

    private Url postmanUrlBuilder(final URL requestUrl) {
        final Url url;
        if (null != requestUrl.getQuery()) {
            url = Url.builder()
                    .host(List.of(requestUrl.getHost().toLowerCase()))
                    .path(getPaths(requestUrl))
                    .raw(requestUrl.toString().toLowerCase())
                    .query(getQueryParams(requestUrl))
                    .protocol(requestUrl.getProtocol().toLowerCase())
                    .build();
        } else {
            url = Url.builder()
                    .host(List.of(requestUrl.getHost()))
                    .path(getPaths(requestUrl))
                    .raw(requestUrl.toString().toLowerCase())
                    .protocol(requestUrl.getProtocol().toLowerCase())
                    .build();
        }
        return url;
    }

    private List<String> getPaths(final URL url) {
        return Arrays.stream(url
                .getPath()
                .toLowerCase()
                .split("/"))
                .filter(path -> !path.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Query> getQueryParams(final URL url) {
        final List<String> queryParams = Arrays.stream(url
                .getQuery()
                .split("&"))
                .filter(path -> !path.isEmpty())
                .collect(Collectors.toList());

        final List<Query> postmanQueryList = new ArrayList<>();

        for (String param : queryParams) {
            String[] params = param.split("=", -1);
            postmanQueryList.add(Query.builder().key(params[0]).value(params[1]).build());
        }
        return postmanQueryList;
    }

    private List<Header> postmanHeadersBuilder(final HttpRequest<?> request){
        final List<Header> postmanHeaders = new ArrayList<>();

        request.getHeaders().all().forEach(h -> {
            if (USER_AGENT.equalsIgnoreCase(h.getName())) {
                postmanHeaders.add(Header.builder().key(h.getName()).value("id-postman/1.0.0").build());
            } else if (!ACCEPT_ENCODING.equalsIgnoreCase(h.getName())) {
                postmanHeaders.add(Header.builder().key(h.getName()).value(h.getValue()).build());
            }
        });
        return postmanHeaders;
    }

    private Body getBody(final HttpRequest<?> request) {
        String processedBody = "";
        Body body = Body.builder().raw(processedBody).mode("raw").build();
        final Optional<kong.unirest.Body> requestBody = request.getBody();

        if (requestBody.isPresent()) {
            if (requestBody.get().multiParts().isEmpty() && null != requestBody.get().uniPart()) {
                processedBody = requestBody.get().uniPart().getValue().toString();
                body.setMode("raw");
                body.setRaw(processedBody);
            } else {
                List<UrlEncoded> urlEncodedList = new ArrayList<>();
                requestBody.get().multiParts().stream()
                        .filter(bodyPart -> bodyPart instanceof ParamPart)
                        .forEach(bodyPart -> urlEncodedList.add(UrlEncoded.builder()
                                .key(bodyPart.getName())
                                .value(bodyPart.getValue().toString())
                                .build()));
                body.setMode("urlencoded");
                body.setUrlEncoded(urlEncodedList);
            }
        }
        return body;
    }

}
