package org.bj.examples.trivia.service.score.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bj.examples.trivia.data.score.ScoreInfo;
import org.bj.examples.trivia.data.score.ScoreInfoRepo;
import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.ScoreException;
import org.bj.examples.trivia.service.score.ScoreService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("mongo")
@Service
public class MongoScoreServiceImpl implements ScoreService {
    private final ScoreInfoRepo scoreInfoRepo;

    public MongoScoreServiceImpl(final ScoreInfoRepo scoreInfoRepo) {
        this.scoreInfoRepo = scoreInfoRepo;
    }

    @Override
    public Map<SlackUser, Long> getAllScoresByUser(final String channelId) {
        final List<ScoreInfo> scores = scoreInfoRepo.findByChannelId(channelId);

        return scores.stream().collect(
                Collectors.toMap(
                        scoreInfo -> new SlackUser(scoreInfo.getUserId(), scoreInfo.getUsername()),
                        ScoreInfo::getScore
                )
        );
    }

    @Override
    public boolean createUserIfNotExists(final String channelId, final SlackUser user) {
        ScoreInfo scoreInfo = scoreInfoRepo.findByChannelIdAndUserId(channelId, user.getUserId());

        if (scoreInfo == null) {
            scoreInfo = new ScoreInfo();
            scoreInfo.setChannelId(channelId);
            scoreInfo.setUserId(user.getUserId());
            scoreInfo.setUsername(user.getUsername());
            scoreInfo.setScore(0L);
            scoreInfoRepo.save(scoreInfo);

            return true;
        }

        return false;
    }

    @Override
    public void incrementScore(final String channelId, final String userId) throws ScoreException {
        ScoreInfo scoreInfo = scoreInfoRepo.findByChannelIdAndUserId(channelId, userId);

        if (scoreInfo == null) {
            throw new ScoreException();
        }

        scoreInfo.setScore(scoreInfo.getScore() + 1);
        scoreInfoRepo.save(scoreInfo);
    }

    @Override
    public void resetScores(final String channelId) {
        scoreInfoRepo.deleteByChannelId(channelId);
    }
}
