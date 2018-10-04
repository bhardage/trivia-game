package org.bj.examples.trivia.message;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DelayedSlackMessageListener {
    private static final Log log = LogFactory.getLog(DelayedSlackMessageListener.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DelayedSlackMessageListener(
            final RestTemplate restTemplate,
            final ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void messageReceiver(String payload) {
        log.info("Message arrived! Payload: " + payload);

        try {
            final DelayedSlackMessage message = objectMapper.readValue(payload, DelayedSlackMessage.class);

            if (message != null && message.getUrl() != null) {
                restTemplate.postForObject(message.getUrl(), message.getResponseDoc(), String.class);
            }
        } catch (IOException e) {
            log.error("An unhandled exception occurred: ", e);
        }
    }
}
