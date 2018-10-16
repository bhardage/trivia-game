package org.bj.examples.trivia.dao.score;

public class ScoreInfo {
    public static final String CHANNEL_ID_KEY = "channelId";
    public static final String USER_ID_KEY = "userId";
    public static final String USERNAME_KEY = "username";
    public static final String SCORE_KEY = "score";

    private Long id;
    private String channelId;
    private String userId;
    private String username;
    private Long score;

    private ScoreInfo(Builder builder) {
        this.id = builder.id;
        this.channelId = builder.channelId;
        this.userId = builder.userId;
        this.username = builder.username;
        this.score = builder.score;
    }

    public static class Builder {
        private Long id;
        private String channelId;
        private String userId;
        private String username;
        private Long score;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder score(Long score) {
            this.score = score;
            return this;
        }

        public ScoreInfo build() {
            return new ScoreInfo(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
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

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }
}
