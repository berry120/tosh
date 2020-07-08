package com.github.berry120.wikiquiz.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.redis.RedisKey;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class RedisService {

    private static final long EXPIRY_SECONDS = 60 * 30; //30 minutes
    private final ObjectMapper mapper;
    private StatefulRedisConnection<String, String> connection;

    public RedisService() {
        mapper = new ObjectMapper();
    }

    public void connect() {
        if (connection == null || !connection.isOpen()) {
            RedisClient client = null;
            connection = client.connect();
        }
    }

    public void set(RedisKey redisKey, Object obj) {
        set(redisKey, obj, true);
    }

    public void setIfAbsent(RedisKey redisKey, Object obj) {
        set(redisKey, obj, false);
    }

    private void set(RedisKey redisKey, Object obj, boolean overwrite) {
        connect();
        try {
            SetArgs args = SetArgs.Builder.ex(EXPIRY_SECONDS);
            if (!overwrite) {
                args = args.nx();
            }
            String key = mapper.writeValueAsString(redisKey);
            String value = mapper.writeValueAsString(obj);
            connection.sync().set(key, value, args);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public <T> Optional<T> get(RedisKey redisKey, TypeReference<T> type) {
        try {
            String json = connection.sync().get(mapper.writeValueAsString(redisKey));
            System.out.println("GETTING " + redisKey + ", " + type);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(mapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> Optional<T> get(RedisKey redisKey, Class<T> clazz) {
        return get(redisKey, new TypeReference<T>() {
        });
    }

}
