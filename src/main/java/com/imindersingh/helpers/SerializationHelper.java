package com.imindersingh.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SerializationHelper {

    private SerializationHelper(){}

    private static final transient Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    public static String asString(final Object object) {
        return GSON.toJson(object);
    }

    public static <T> T asObject(final String body, final Class<T> classOfT) {
        return GSON.fromJson(body, classOfT);
    }

}
