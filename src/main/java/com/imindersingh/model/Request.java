package com.imindersingh.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class Request {

    private final Body body;
    private final String description;
    private final List<Header> header;
    private final String method;
    private final Url url;

}
