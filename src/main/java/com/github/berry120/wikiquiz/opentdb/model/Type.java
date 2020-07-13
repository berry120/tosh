package com.github.berry120.wikiquiz.opentdb.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Type {

    MULTIPLE_CHOICE("multiple"),
    TRUE_FALSE("boolean");

    private final String type;

    public String toParamValue() {
        return type;
    }

}
