package org.bj.examples.trivia.service.score.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bj.examples.trivia.data.score.ScoreInfo;
import org.bj.examples.trivia.data.score.ScoreInfoDao;
import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.ScoreException;
import org.bj.examples.trivia.service.score.ScoreService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("production")
@Service
public class ScoreServiceImpl implements ScoreService {
    private final ScoreInfoDao scoreInfoDao;

    public ScoreServiceImpl(final ScoreInfoDao scoreInfoDao) {
        this.scoreInfoDao = scoreInfoDao;
    }

    @Override
    public Map<SlackUser, Long> getAllScoresByUser(final String channelId) {
        final List<ScoreInfo> scores = scoreInfoDao.findAllByChannelId(channelId);

        return scores.stream().collect(
                Collectors.toMap(
                        scoreInfo -> new SlackUser(scoreInfo.getUserId(), scoreInfo.getUsername()),
                        ScoreInfo::getScore
                )
        );
    }

    @Override
    public boolean createUserIfNotExists(final String channelId, final SlackUser user) {
        ScoreInfo scoreInfo = scoreInfoDao.findByChannelIdAndUserId(channelId, user.getUserId());

        if (scoreInfo == null) {
            scoreInfo = new ScoreInfo();
            scoreInfo.setChannelId(channelId);
            scoreInfo.setUserId(user.getUserId());
            scoreInfo.setUsername(user.getUsername());
            scoreInfo.setScore(0L);
            scoreInfoDao.save(scoreInfo);

            return true;
        }

        return false;
    }

    @Override
    public boolean doesUserExist(final String channelId, final String userId) {
        final ScoreInfo scoreInfo = scoreInfoDao.findByChannelIdAndUserId(channelId, userId);
        return scoreInfo != null;
    }

    @Override
    public void incrementScore(final String channelId, final String userId) throws ScoreException {
        final ScoreInfo scoreInfo = scoreInfoDao.findByChannelIdAndUserId(channelId, userId);

        if (scoreInfo == null) {
            throw new ScoreException();
        }

        scoreInfo.setScore(scoreInfo.getScore() + 1);
        scoreInfoDao.save(scoreInfo);
    }

    @Override
    public void resetScores(final String channelId) {
        scoreInfoDao.deleteAllByChannelId(channelId);
    }
}
