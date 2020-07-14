package com.github.berry120.wikiquiz.service;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnswerTransformerService {

    public Map<String, List<String>> answersToClientFormat(Map<String, String> rawAnswers) {
        Set<String> answers = new HashSet<>(rawAnswers.values());
        Map<String, List<String>> ret = new HashMap<>();

        for (String answer : answers) {
            ret.put(answer, rawAnswers.entrySet().stream()
                    .filter(e -> e.getValue().equals(answer))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
        }

        return ret;
    }

}
