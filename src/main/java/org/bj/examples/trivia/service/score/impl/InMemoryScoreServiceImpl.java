package org.bj.examples.trivia.service.score.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.ScoreException;
import org.bj.examples.trivia.service.score.ScoreService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("memory")
@Service
public class InMemoryScoreServiceImpl implements ScoreService {
    private Map<SlackUser, Long> scoresByUser = new ConcurrentHashMap<>();

    @Override
    public Map<SlackUser, Long> getAllScoresByUser(final String channelId) {
        return new HashMap<SlackUser, Long>(scoresByUser);
    }

    @Override
    public boolean createUserIfNotExists(final String channelId, final SlackUser user) {
        if (user != null && !scoresByUser.containsKey(user)) {
            scoresByUser.put(user, 0L);

            return true;
        }

        return false;
    }

    @Override
    public boolean doesUserExist(final String channelId, final String userId) {
        return userId != null && scoresByUser.containsKey(new SlackUser(userId, null));
    }

    @Override
    public void incrementScore(final String channelId, final String userId) throws ScoreException {
        final SlackUser key = new SlackUser(userId, null);

        if (userId == null || !scoresByUser.containsKey(key)) {
            throw new ScoreException();
        }

        scoresByUser.put(key, scoresByUser.get(key) + 1);
    }

    @Override
    public void resetScores(final String channelId) {
        scoresByUser.clear();
    }
}
