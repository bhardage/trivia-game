package org.bj.examples.trivia.dao.workflow;

import java.time.LocalDateTime;

public class Answer {
    public static final String USER_ID_KEY = "userId";
    public static final String USERNAME_KEY = "username";
    public static final String TEXT_KEY = "text";
    public static final String CREATED_DATE_KEY = "createdDate";

    private String userId;
    private String username;
    private String text;
    private LocalDateTime createdDate;

    private Answer(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.text = builder.text;
        this.createdDate = builder.createdDate;
    }

    public static class Builder {
        private String userId;
        private String username;
        private String text;
        private LocalDateTime createdDate;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Answer build() {
            return new Answer(this);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
