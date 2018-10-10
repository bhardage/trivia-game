package org.bj.examples.trivia.service.workflow;

import org.bj.examples.trivia.exception.WorkflowException;

public interface WorkflowService {
    void onGameStarted(final String channelId, final String userId) throws WorkflowException;
    void onGameStopped(final String channelId, final String userId) throws WorkflowException;
    void onQuestionSubmission(final String channelId, final String userId) throws WorkflowException;
    void onAnswerSubmission(final String channelId, final String userId) throws WorkflowException;
    void onCorrectAnswer(final String channelId, final String userId) throws WorkflowException;
    void onTurnChange(final String channelId, final String userId, final String newControllingUserId) throws WorkflowException;
}
