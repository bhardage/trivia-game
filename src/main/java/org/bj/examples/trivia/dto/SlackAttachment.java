package org.bj.examples.trivia.dto;

public class SlackAttachment {
    private String text;

    public SlackAttachment(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
