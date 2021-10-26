package com.imindersingh.requests;

import com.github.sitture.env.config.EnvConfig;
import com.imindersingh.store.AbstractPostmanStore;

public final class PostmanRequestStoreConfig {

    public static final String POSTMAN_COLLECTION_STORE_NAME;
    public static final String POSTMAN_COLLECTION_STORE_PATH;

    private PostmanRequestStoreConfig() { }

    static {
        POSTMAN_COLLECTION_STORE_NAME = String.format("postman_%s.db", AbstractPostmanStore.getDate());
        POSTMAN_COLLECTION_STORE_PATH = EnvConfig.get("POSTMAN_COLLECTION_STORE_PATH") == null
                ? "reports"
                : EnvConfig.get("POSTMAN_COLLECTION_STORE_PATH");
    }

}
