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
