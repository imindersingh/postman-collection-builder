package com.imindersingh.requests;

import com.github.sitture.env.config.EnvConfig;
import kong.unirest.Unirest;

import static com.imindersingh.requests.PostmanRequestStoreConfig.POSTMAN_COLLECTION_STORE_NAME;
import static com.imindersingh.requests.PostmanRequestStoreConfig.POSTMAN_COLLECTION_STORE_PATH;

public class RequestConfig {

    static {
        if (EnvConfig.getBool("POSTMAN_COLLECTION")) {
            Unirest.config()
                    .interceptor(new PostmanRequestCollector(new PostmanRequestStore(POSTMAN_COLLECTION_STORE_NAME,
                            POSTMAN_COLLECTION_STORE_PATH)));
        }
    }
}
