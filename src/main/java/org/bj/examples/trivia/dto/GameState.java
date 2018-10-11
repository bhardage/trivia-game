package org.bj.examples.trivia.dto;

public class GameState {
    private String controllingUserId;
    private String question;

    public GameState(final String controllingUserId, final String question) {
        this.controllingUserId = controllingUserId;
        this.question = question;
    }

    public String getControllingUserId() {
        return controllingUserId;
    }

    public void setControllingUserId(String controllingUserId) {
        this.controllingUserId = controllingUserId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
