package org.bj.examples.trivia.data.score;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class ScoreInfo {
    public static final String CHANNEL_ID_KEY = "channelId";
    public static final String USER_ID_KEY = "userId";
    public static final String USERNAME_KEY = "username";
    public static final String SCORE_KEY = "score";

    @Id
    private ObjectId id;
    private String channelId;
    private String userId;
    private String username;
    private Long score;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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
