package org.bj.examples.trivia.message;

import org.bj.examples.trivia.dto.SlackResponseDoc;

public class DelayedSlackMessage {
    private String url;
    private SlackResponseDoc responseDoc;

    public DelayedSlackMessage(final String url, final SlackResponseDoc responseDoc) {
        this.url = url;
        this.responseDoc = responseDoc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SlackResponseDoc getResponseDoc() {
        return responseDoc;
    }

    public void setResponseDoc(SlackResponseDoc responseDoc) {
        this.responseDoc = responseDoc;
    }
}
