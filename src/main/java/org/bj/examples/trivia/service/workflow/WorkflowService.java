package org.bj.examples.trivia.service.workflow;

import org.bj.examples.trivia.exception.GameNotStartedException;
import org.bj.examples.trivia.exception.WorkflowException;

public interface WorkflowService {
    void onGameStarted(final String channelId, final String userId) throws WorkflowException;
    void onGameStopped(final String channelId, final String userId) throws GameNotStartedException, WorkflowException;
    void onQuestionSubmitted(final String channelId, final String userId, final String question) throws GameNotStartedException, WorkflowException;
    void onAnswerSubmitted(final String channelId, final String userId) throws GameNotStartedException, WorkflowException;
    void onCorrectAnswerSelected(final String channelId, final String userId) throws GameNotStartedException, WorkflowException;
    void onTurnChanged(final String channelId, final String userId, final String newControllingUserId)
            throws GameNotStartedException, WorkflowException;
}
