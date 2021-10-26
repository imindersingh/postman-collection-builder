package com.imindersingh.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class Item {

    @SerializedName("_postman_isSubFolder")
    private final boolean postmanIsSubFolder;
    private final String description;
    @SerializedName("item")
    private final List<Item> items;
    private final String name;
    private final Request request;

}
