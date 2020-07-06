package com.github.berry120.wikiquiz.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonScore {

    private String name;
    private long score;

}
