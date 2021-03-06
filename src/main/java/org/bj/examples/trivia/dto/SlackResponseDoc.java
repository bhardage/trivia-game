package org.bj.examples.trivia.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class SlackResponseDoc {
    @JsonProperty(value = "response_type")
    private SlackResponseType responseType;

    private String text;
    private List<SlackAttachment> attachments;

    public static SlackResponseDoc failure(final String text) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.responseType = SlackResponseType.EPHEMERAL;
        responseDoc.text = text;
        return responseDoc;
    }

    public SlackResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(SlackResponseType responseType) {
        this.responseType = responseType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<SlackAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<SlackAttachment> attachments) {
        this.attachments = attachments;
    }
}
