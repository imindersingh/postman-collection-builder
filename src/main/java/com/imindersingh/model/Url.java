package com.imindersingh.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Url {

    private List<String> host;
    private List<String> path;
    private String raw;
    private String protocol;
    private List<Query> query;
    private List<Variable> variable;

}
