package org.bj.examples.trivia.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.ScoreException;
import org.bj.examples.trivia.service.ScoreService;
import org.springframework.stereotype.Service;

@Service
public class InMemoryScoreServiceImpl implements ScoreService {
    private Map<SlackUser, Long> scoresByUser = new ConcurrentHashMap<>();

    @Override
    public Map<SlackUser, Long> getAllScoresByUser() {
        return new HashMap<SlackUser, Long>(scoresByUser);
    }

    @Override
    public void createUserIfNotExists(final SlackUser user) {
        if (user == null) {
            return;
        }

        if (!scoresByUser.containsKey(user)) {
            scoresByUser.put(user, 0L);
        }
    }

    @Override
    public void incrementScore(final String userId) throws ScoreException {
        final SlackUser key = new SlackUser(userId, null);

        if (userId == null || !scoresByUser.containsKey(key)) {
            throw new ScoreException();
        }

        scoresByUser.put(key, scoresByUser.get(key) + 1);
    }

    @Override
    public void resetScores() {
        scoresByUser.clear();
    }
}