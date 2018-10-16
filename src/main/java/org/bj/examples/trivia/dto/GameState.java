package org.bj.examples.trivia.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class GameState {
    private final String controllingUserId;
    private final String question;
    private final List<Answer> answers;

    public GameState(final String controllingUserId, final String question, final List<Answer> answers) {
        this.controllingUserId = controllingUserId;
        this.question = question;
        this.answers = answers;
    }

    public String getControllingUserId() {
        return controllingUserId;
    }

    public String getQuestion() {
        return question;
    }

    public List<Answer> getAnswers() {
        return answers;
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
