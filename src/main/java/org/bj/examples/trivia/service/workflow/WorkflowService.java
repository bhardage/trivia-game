package org.bj.examples.trivia.service.workflow;

import java.time.LocalDateTime;

import org.bj.examples.trivia.dto.GameState;
import org.bj.examples.trivia.exception.GameNotStartedException;
import org.bj.examples.trivia.exception.WorkflowException;

public interface WorkflowService {
    void onGameStarted(final String channelId, final String userId, final String topic) throws WorkflowException;
    void onGameStopped(final String channelId, final String userId) throws GameNotStartedException, WorkflowException;
    void onQuestionSubmitted(final String channelId, final String userId, final String question) throws GameNotStartedException, WorkflowException;
    void onAnswerSubmitted(
            final String channelId,
            final String userId,
            final String username,
            final String answerText,
            final LocalDateTime createdDate
    ) throws GameNotStartedException, WorkflowException;
    void onCorrectAnswerSelected(final String channelId, final String userId) throws GameNotStartedException, WorkflowException;
    void onTurnChanged(final String channelId, final String userId, final String newControllingUserId)
            throws GameNotStartedException, WorkflowException;
    GameState getCurrentGameState(final String channelId);
}
