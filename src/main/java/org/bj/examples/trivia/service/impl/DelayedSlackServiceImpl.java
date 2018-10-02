package org.bj.examples.trivia.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.message.DelayedSlackMessage;
import org.bj.examples.trivia.message.PubsubOutboundGateway;
import org.bj.examples.trivia.service.DelayedSlackService;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DelayedSlackServiceImpl implements DelayedSlackService {
    private static final Log log = LogFactory.getLog(DelayedSlackServiceImpl.class);

    private final PubsubOutboundGateway messagingGateway;
    private final ObjectMapper objectMapper;

    public DelayedSlackServiceImpl(
            final PubsubOutboundGateway messagingGateway,
            final ObjectMapper objectMapper
    ) {
        this.messagingGateway = messagingGateway;
        this.objectMapper = objectMapper;
    }

    public void sendResponse(final String url, final SlackResponseDoc responseDoc) {
        try {
            final DelayedSlackMessage message = new DelayedSlackMessage(url, responseDoc);
            final String messageText = objectMapper.writeValueAsString(message);

            log.info("Sending message to URL \"" + url + "\": " + messageText);

            messagingGateway.sendToPubsub(messageText);
        } catch (JsonProcessingException e) {
            log.error("An unhandled exception occurred: ", e);
        }
    }
}
