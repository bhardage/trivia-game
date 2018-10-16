package org.bj.examples.trivia.service.slack.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.service.slack.DelayedSlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DelayedSlackServiceImpl implements DelayedSlackService {
    private static final Log log = LogFactory.getLog(DelayedSlackServiceImpl.class);

    private final RestTemplate restTemplate;

    @Autowired
    public DelayedSlackServiceImpl(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendResponse(final String url, final SlackResponseDoc responseDoc) {
        log.info("Sending message to URL \"" + url + "\".");

        new Thread(() -> restTemplate.postForObject(url, responseDoc, String.class)).start();
    }
}
