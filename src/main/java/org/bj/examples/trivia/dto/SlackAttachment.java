package org.bj.examples.trivia.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SlackAttachment {
    private final String text;
    private final List<String> mrkdwn_in;

    public SlackAttachment(final String text) {
        this.text = text;
        this.mrkdwn_in = Arrays.asList("text");
    }

    public SlackAttachment(final String text, final boolean allowMarkdown) {
        this.text = text;
        this.mrkdwn_in = allowMarkdown ? Arrays.asList("text") : new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public List<String> getMrkdwn_in() {
        return mrkdwn_in;
    }
}
