package com.github.berry120.wikiquiz.opentdb.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Encoding {

    URL("url3986"),
    LEGACY_URL("urlLegacy"),
    BASE_64("base64");

    private final String encoding;

    public String toParamValue() {
        return encoding;
    }

}
