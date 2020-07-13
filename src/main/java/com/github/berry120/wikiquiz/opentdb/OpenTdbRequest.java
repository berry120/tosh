package com.github.berry120.wikiquiz.opentdb;

import com.github.berry120.wikiquiz.opentdb.model.Category;
import com.github.berry120.wikiquiz.opentdb.model.Difficulty;
import com.github.berry120.wikiquiz.opentdb.model.Encoding;
import com.github.berry120.wikiquiz.opentdb.model.Type;
import lombok.Builder;

@Builder
public class OpenTdbRequest {

    public static final String ENDPOINT = "https://opentdb.com/api.php";
    private final int numQuestions;
    private final Category category;
    private final Difficulty difficulty;
    private final Type type;
    private final Encoding encoding;

    public String getRequestUrl() {
        StringBuilder ret = new StringBuilder(ENDPOINT);
        ret.append("?amount=").append(numQuestions);
        if (category != null) {
            ret.append("&category=").append(category.toParamValue());
        }
        if (difficulty != null) {
            ret.append("&difficulty=").append(difficulty.toParamValue());
        }
        if (type != null) {
            ret.append("&type=").append(type.toParamValue());
        }
        if (encoding != null) {
            ret.append("&encode=").append(encoding.toParamValue());
        }
        return ret.toString();
    }

}
