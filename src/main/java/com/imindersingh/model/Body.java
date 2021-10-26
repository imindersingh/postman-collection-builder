package com.imindersingh.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Body {

    private String mode;
    private String raw;
    @SerializedName("urlencoded")
    private List<UrlEncoded> urlEncoded;

}
