package org.bj.examples.trivia.service.game;

import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;

public interface TriviaGameService {
    SlackResponseDoc start(final SlackRequestDoc requestDoc);

    /**
     * This method is used when a person is supposed to be
     * selecting a quote but they don't want to
     */
    SlackResponseDoc stop(final SlackRequestDoc requestDoc);

    SlackResponseDoc submitQuestion(final SlackRequestDoc requestDoc, final String question);
    SlackResponseDoc submitAnswer(final SlackRequestDoc requestDoc, final String answer);
    SlackResponseDoc markAnswerCorrect(final SlackRequestDoc requestDoc, final String target, final String answer);

    SlackResponseDoc getScores(final SlackRequestDoc requestDoc);
    SlackResponseDoc resetScores(final SlackRequestDoc requestDoc);
}
