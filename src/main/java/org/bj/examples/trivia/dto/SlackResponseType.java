package org.bj.examples.trivia.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SlackResponseType {
    IN_CHANNEL("in_channel"),
    EPHEMERAL("ephemeral");
    
    private final String name;
    
    private SlackResponseType(final String name) {
        this.name = name;
    }
    
    @JsonValue
    public String getName() {
        return name;
    }
}
