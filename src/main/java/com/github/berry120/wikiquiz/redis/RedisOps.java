package com.github.berry120.wikiquiz.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.redis.model.RedisKey;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class RedisOps {

    private static final long EXPIRY_SECONDS = 60 * 60 * 6; //6 hours
    private final ObjectMapper mapper;
    @ConfigProperty(name = "redis.host")
    String redisHost;
    @ConfigProperty(name = "redis.pwd")
    String redisPwd;
    @ConfigProperty(name = "redis.port")
    String redisPort;
    private StatefulRedisConnection<String, String> connection;

    RedisOps() {
        mapper = new ObjectMapper();
    }

    @PostConstruct
    void connect() {
        if (connection == null || !connection.isOpen()) {
            RedisClient client = RedisClient.create("redis://" + redisPwd + "@" + redisHost + ":" + redisPort);
            connection = client.connect();
        }
    }

    @PreDestroy
    void close() {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    <T> Optional<T> get(RedisKey redisKey, TypeReference<T> type) {
        try {
            String json = connection.sync().get(mapper.writeValueAsString(redisKey));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(mapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    void set(RedisKey redisKey, Object obj) {
        System.out.println("SETTING " + redisKey + " to " + obj);
        try {
            SetArgs args = SetArgs.Builder.ex(EXPIRY_SECONDS);
            String key = mapper.writeValueAsString(redisKey);
            String value = mapper.writeValueAsString(obj);
            connection.sync().set(key, value, args);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    void delete(RedisKey redisKey) {
        System.out.println("DELETING " + redisKey);
        try {
            String key = mapper.writeValueAsString(redisKey);
            connection.sync().del(key);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
