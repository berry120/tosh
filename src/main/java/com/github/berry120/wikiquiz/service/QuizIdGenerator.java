package com.github.berry120.wikiquiz.service;

import javax.enterprise.context.ApplicationScoped;
import java.util.Random;

@ApplicationScoped
public class QuizIdGenerator {

    private final Random random;

    public QuizIdGenerator() {
        random = new Random();
    }

    public String generateRandomId() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
