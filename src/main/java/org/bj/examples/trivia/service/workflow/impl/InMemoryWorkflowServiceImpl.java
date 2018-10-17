package org.bj.examples.trivia.service.workflow.impl;

import java.time.LocalDateTime;

import org.bj.examples.trivia.dto.GameState;
import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.GameNotStartedException;
import org.bj.examples.trivia.exception.WorkflowException;
import org.bj.examples.trivia.service.workflow.WorkflowService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("memory")
@Service
public class InMemoryWorkflowServiceImpl implements WorkflowService {
    private SlackUser currentHost = null;
    private String question = null;

    @Override
    public void onGameStarted(final String channelId, final String userId, final String topic) throws WorkflowException {
        if (userId == null) {
            return;
        }

        final SlackUser user = new SlackUser(userId, null);

        if (currentHost != null) {
            final String message = currentHost.equals(user) ?
                    "You are already hosting!" :
                    "<@" + currentHost.getUserId() + "> is currently hosting.";

            throw new WorkflowException(message);
        }

        currentHost = user;
    }

    @Override
    public void onGameStopped(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (userId == null) {
            return;
        }

        if (currentHost == null) {
            throw new GameNotStartedException();
        } else if (currentHost.getUserId().equals(userId)) {
            throw new WorkflowException("<@" + currentHost.getUserId() + "> is currently hosting.");
        }

        currentHost = null;
        question = null;
    }

    @Override
    public void onQuestionSubmitted(final String channelId, final String userId, final String question) throws GameNotStartedException, WorkflowException {
        if (userId == null) {
            return;
        }

        if (currentHost == null) {
            throw new GameNotStartedException();
        } else {
            boolean isControllingUser = currentHost.getUserId().equals(userId);

            if (this.question != null) {
                throw new WorkflowException((isControllingUser ? "You have" : "<@" + currentHost.getUserId() + "> has") + " already asked a question.");
            } else if (!isControllingUser) {
                throw new WorkflowException("It's <@" + currentHost.getUserId() + ">'s turn to ask a question.");
            }
        }

        this.question = question;
    }

    @Override
    public void onAnswerSubmitted(
            final String channelId,
            final String userId,
            final String username,
            final String answerText,
            final LocalDateTime createdDate
    ) throws GameNotStartedException, WorkflowException {
        if (userId == null) {
            return;
        }

        if (currentHost == null) {
            throw new GameNotStartedException();
        } else if (currentHost.getUserId().equals(userId)) {
            throw new WorkflowException("You can't answer your own question!");
        } else if (question == null) {
            throw new WorkflowException("A question has not yet been submitted. Please wait for <@" + currentHost.getUserId() + "> to ask a question.");
        }
    }

    @Override
    public void onCorrectAnswerSelected(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (userId == null) {
            return;
        }

        if (currentHost == null) {
            throw new GameNotStartedException();
        } else if (!currentHost.getUserId().equals(userId)) {
            throw new WorkflowException("It's <@" + currentHost.getUserId() + ">'s question. Only he/she can mark an answer correct.");
        } else if (question == null) {
            throw new WorkflowException("A question has not yet been submitted. Please ask a question before marking an answer correct.");
        }
    }

    @Override
    public void onTurnChanged(final String channelId, final String userId, final String newControllingUserId)
            throws GameNotStartedException, WorkflowException {
        if (userId == null || newControllingUserId == null) {
            return;
        }

        if (currentHost == null) {
            throw new GameNotStartedException();
        } else if (!currentHost.getUserId().equals(userId)) {
            throw new WorkflowException("It's <@" + currentHost.getUserId() + ">'s turn; only he/she can cede his/her turn.");
        }

        currentHost = new SlackUser(newControllingUserId, null);
        question = null;
    }

    @Override
    public GameState getCurrentGameState(final String channelId) {
        final GameState gameState = new GameState();
        gameState.setControllingUserId(currentHost == null ? null : currentHost.getUserId());
        gameState.setQuestion(question);

        return gameState;
    }
}
