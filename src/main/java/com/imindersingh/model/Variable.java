package com.imindersingh.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class Variable {

    private final String key;
    private final String value;

}