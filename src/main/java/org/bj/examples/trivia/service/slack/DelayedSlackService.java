package org.bj.examples.trivia.service.slack;

import org.bj.examples.trivia.dto.SlackResponseDoc;

public interface DelayedSlackService {
    void sendResponse(final String url, final SlackResponseDoc responseDoc);
}
