package com.imindersingh.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class Info {

    @SerializedName("_postman_id")
    private final String postmanId;
    private final String name;
    private final String schema;

}
