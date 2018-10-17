package org.bj.examples.trivia.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class GameState {
    private String controllingUserId;
    private String topic;
    private String question;
    private List<Answer> answers;

    public String getControllingUserId() {
        return controllingUserId;
    }

    public void setControllingUserId(String controllingUserId) {
        this.controllingUserId = controllingUserId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public static final class Answer {
        private final String userId;
        private final String username;
        private final String text;
        private final LocalDateTime createdDate;

        public Answer(
                final String userId,
                final String username,
                final String text,
                final LocalDateTime createdDate
        ) {
            this.userId = userId;
            this.username = username;
            this.text = text;
            this.createdDate = createdDate;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getText() {
            return text;
        }

        public LocalDateTime getCreatedDate() {
            return createdDate;
        }
    }
}
