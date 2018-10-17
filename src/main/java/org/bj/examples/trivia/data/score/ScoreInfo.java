package org.bj.examples.trivia.data.score;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@CompoundIndexes({
    @CompoundIndex(name = "channelId_userId", def = "{'channelId' : 1, 'userId': 1}", unique = true)
})
public class ScoreInfo {
    public static final String CHANNEL_ID_KEY = "channelId";
    public static final String USER_ID_KEY = "userId";
    public static final String USERNAME_KEY = "username";
    public static final String SCORE_KEY = "score";

    @Id
    private ObjectId id;

    @Indexed
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
