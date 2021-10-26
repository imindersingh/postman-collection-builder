package com.imindersingh.collection;

import com.imindersingh.helpers.SerializationHelper;
import com.imindersingh.model.Collection;
import com.imindersingh.model.Header;
import com.imindersingh.model.Info;
import com.imindersingh.model.Item;
import com.imindersingh.model.Query;
import com.imindersingh.model.Request;
import com.imindersingh.model.Variable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.imindersingh.store.AbstractPostmanStore.getDate;

public class PostmanCollectionBuilder {

    private static final String PARAM = ":param";
    private static final String SCHEMA = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
    private static final Predicate<Header> COUNTRY_HEADER_FILTER = header -> header.getKey().equalsIgnoreCase("Country");
    private static final Predicate<Header> CITY_HEADER_FILTER = header -> header.getKey().equalsIgnoreCase("City");

    @Getter private final String collectionName;
    private final String host;

    public PostmanCollectionBuilder(String environment, final String application, final String host) {
        this.host = host;
        collectionName = String.format("%s-%s - v%s", application.toLowerCase(), environment.toLowerCase(), getDate());
    }

    public Collection createPostmanCollection(final List<String> storeRequests) {
        List<Request> requests = getStoreRequestList(storeRequests);
        requests = getRequestsFilteredByHost(requests);
        requests = getUniqueRequests(requests);

        final Map<String, List<Item>> mappedRequests = mapRequestsToCityCountry(requests);
        final List<Item> items = createCollectionFolderStructure(mappedRequests);

        return Collection.builder()
                .info(info())
                .item(items)
                .build();
    }

    public String getCollectionJsonStr(final Collection collection) {
        return SerializationHelper.asString(collection);
    }

    private List<Request> getStoreRequestList(final List<String> requests) {
        final List<Request> requestList = new ArrayList<>();

        for (String request : requests) {
            requestList.add(SerializationHelper.asObject(request, Request.class));
        }
        return requestList;
    }

    private List<Request> getRequestsFilteredByHost(final List<Request> requests) {
        return requests.stream()
                .filter(request -> request.getUrl().getRaw().contains(host))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Request> getUniqueRequests(List<Request> requestList) {
        for (Request request : requestList) {
            final List<String> paths = new ArrayList<>(request.getUrl().getPath());
            final Pattern pattern = Pattern.compile("([0-9.@]+)");

            final List<Variable> postmanVariables = new ArrayList<>();

            for (String path: paths) {
                Matcher matcher = pattern.matcher(path);
                if (matcher.find()) {
                    paths.set(paths.indexOf(path), PARAM);
                    postmanVariables.add(Variable.builder().key(PARAM).value(path).build());
                }
            }

            request.getUrl().setPath(paths);
            request.getUrl().setVariable(postmanVariables);

            final String url = getUrl(request, paths);
            request.getUrl().setRaw(url);
        }

        return requestList.stream()
                .filter(distinctByKey(request -> request.getUrl().getPath()
                        + getHeaderValue(request, COUNTRY_HEADER_FILTER).toUpperCase()
                        + getHeaderValue(request, CITY_HEADER_FILTER).toUpperCase()
                        + request.getMethod()))
                .sorted(Comparator.comparing(request -> request.getUrl().getRaw()))
                .collect(Collectors.toList());
    }

    private Map<String, List<Item>> mapRequestsToCityCountry(final List<Request> requests) {
        final Map<String, List<Item>> cityCountryRequests = new HashMap<>();

        for (Request request : requests) {

            String city = getHeaderValue(request, CITY_HEADER_FILTER).toUpperCase();
            if ("NONE".equals(city)) {
                city = "NO_CITY";
            }

            String country = getHeaderValue(request, COUNTRY_HEADER_FILTER).toUpperCase();
            if ("NONE".equals(country)) {
                country = "NO_COUNTRY";
            }

            final List<Item> requestItemList = new ArrayList<>();
            final Item requestItem = Item.builder().name(paramBuilder(request.getUrl().getPath())).request(request).build();
            requestItemList.add(requestItem);

            final String cityCountry = String.format("%s-%s", city, country);

            if (cityCountryRequests.containsKey(cityCountry)) {
                cityCountryRequests.get(cityCountry).add(requestItem);
            } else {
                cityCountryRequests.put(cityCountry, requestItemList);
            }
        }
        return cityCountryRequests;
    }

    private List<Item> createCollectionFolderStructure(final Map<String, List<Item>> cityCountry) {
        final List<Item> rootFolders = new ArrayList<>();
        for (Map.Entry<String, List<Item>> set : cityCountry.entrySet()) {
            final String[] cityCountryKey = set.getKey().split("-");
            final String city = cityCountryKey[0];
            final String country = cityCountryKey[1];

            final Item subFolder = Item.builder().name(country).items(set.getValue()).postmanIsSubFolder(true).build();
            final List<Item> subFolderList = new ArrayList<>();
            subFolderList.add(subFolder);

            if (rootFolders.stream().anyMatch(rootFolder -> rootFolder.getName().equals(city))) {
                for (Item rootFolder : rootFolders) {
                    if (rootFolder.getName().equals(city)) {
                        rootFolder.getItems().add(subFolder);
                        break;
                    }
                }
            } else {
                rootFolders.add(Item.builder().name(city).items(subFolderList).build());
            }
        }
        return rootFolders;
    }

    private String getUrl(final Request request, final List<String> pathList) {
        final String url;
        if (null != request.getUrl().getQuery()) {
            request.getUrl().getQuery().forEach(query -> request.getUrl().getVariable().add(
                    Variable.builder()
                            .key(query.getKey())
                            .value(query.getValue())
                            .build()
            ));
            url = String.format("%s://%s%s%s",
                    request.getUrl().getProtocol(),
                    request.getUrl().getHost().get(0),
                    paramBuilder(pathList),
                    queryBuilder(request.getUrl().getQuery()));
        } else {
            url = String.format("%s://%s%s",
                    request.getUrl().getProtocol(),
                    request.getUrl().getHost().get(0),
                    paramBuilder(pathList));
        }
        return url;
    }

    private String paramBuilder(final List<String> paramList) {
        final StringBuilder paramBuilder = new StringBuilder();
        for (String param: paramList) {
            paramBuilder.append(String.format("/%s", param));
        }
        return paramBuilder.toString();
    }

    private String getHeaderValue(final Request request, final Predicate<Header> filter) {
        return request
                .getHeader()
                .stream()
                .filter(filter)
                .map(Header::getValue)
                .findFirst()
                .orElse("NONE");
    }

    private String queryBuilder(final List<Query> queryList) {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append('?');
        for (Query query : queryList) {
            queryBuilder.append(String.format("%s=%s", query.getKey(), query.getValue()));
            if (queryList.indexOf(query) != queryList.size() - 1) {
                queryBuilder.append('&');
            }
        }
        return queryBuilder.toString();
    }

    private Info info() {
        return Info.builder()
                .postmanId(UUID.randomUUID().toString())
                .name(collectionName)
                .schema(SCHEMA)
                .build();
    }

    private static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
        Set<Object> map = ConcurrentHashMap.newKeySet();
        return t -> map.add(keyExtractor.apply(t));
    }
}
