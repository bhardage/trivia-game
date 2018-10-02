package org.bj.examples.trivia.service;

import java.util.Map;

import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.ScoreException;

public interface ScoreService {
    Map<SlackUser, Long> getAllScoresByUser();
    void createUserIfNotExists(final SlackUser user);
    void incrementScore(final String userId) throws ScoreException;
    void resetScores();
}
