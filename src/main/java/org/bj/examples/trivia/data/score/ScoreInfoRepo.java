package org.bj.examples.trivia.data.score;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScoreInfoRepo extends MongoRepository<ScoreInfo, String> {
    List<ScoreInfo> findByChannelId(final String channelId);
    ScoreInfo findByChannelIdAndUserId(final String channelId, final String userId);
    void deleteByChannelId(final String channelId);
}
