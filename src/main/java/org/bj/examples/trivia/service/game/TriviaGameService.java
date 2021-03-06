package org.bj.examples.trivia.service.game;

import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;

public interface TriviaGameService {
    SlackResponseDoc start(final SlackRequestDoc requestDoc, final String topic);

    /**
     * This method is used when a person is supposed to be
     * selecting a quote but they don't want to
     */
    SlackResponseDoc stop(final SlackRequestDoc requestDoc);

    /**
     * This method allows users to participate in playing the
     * game. Note that a game does not have to be started to join
     */
    SlackResponseDoc join(final SlackRequestDoc requestDoc);

    SlackResponseDoc pass(final SlackRequestDoc requestDoc, final String target);

    SlackResponseDoc submitQuestion(final SlackRequestDoc requestDoc, final String question);
    SlackResponseDoc submitAnswer(final SlackRequestDoc requestDoc, final String answer);
    SlackResponseDoc markAnswerCorrect(final SlackRequestDoc requestDoc, final String target, final String answer);

    SlackResponseDoc getStatus(final SlackRequestDoc requestDoc);

    SlackResponseDoc getScores(final SlackRequestDoc requestDoc);
    SlackResponseDoc resetScores(final SlackRequestDoc requestDoc);
}
