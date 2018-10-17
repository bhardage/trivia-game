package org.bj.examples.trivia.service.score;

import java.util.Map;

import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.ScoreException;

public interface ScoreService {
    Map<SlackUser, Long> getAllScoresByUser(final String channelId);
    boolean createUserIfNotExists(final String channelId, final SlackUser user);
    void incrementScore(final String channelId, final String userId) throws ScoreException;
    void resetScores(final String channelId);
}
