package com.github.berry120.wikiquiz.opentdb;

import com.github.berry120.wikiquiz.opentdb.model.Category;
import com.github.berry120.wikiquiz.opentdb.model.Difficulty;
import com.github.berry120.wikiquiz.opentdb.model.Type;
import lombok.Builder;

@Builder
public class OpenTdbRequest {

    public static final String ENDPOINT = "https://opentdb.com/api.php";
    private final int numQuestions;
    private final Category category;
    private final Difficulty difficulty;
    private final Type type;

    public String getRequestUrl() {
        StringBuilder ret = new StringBuilder(ENDPOINT);
        ret.append("?amount=" + numQuestions);
        if (category != null) {
            ret.append("&category=" + category.toParamValue());
        }
        if (difficulty != null) {
            ret.append("&difficulty=" + difficulty.toParamValue());
        }
        if (type != null) {
            ret.append("&type=" + type.toParamValue());
        }
        return ret.toString();
    }

}
