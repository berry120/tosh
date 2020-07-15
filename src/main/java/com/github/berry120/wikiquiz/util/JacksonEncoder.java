package com.github.berry120.wikiquiz.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

public class JacksonEncoder implements Encoder.Text<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * No explicit initialisation required
     */
    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    /**
     * No explicit destruction required
     */
    @Override
    public void destroy() {

    }

    @Override
    public String encode(Object o) throws EncodeException {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (IOException e) {
            throw new EncodeException(o, "Could not encode.", e);
        }
    }
}
