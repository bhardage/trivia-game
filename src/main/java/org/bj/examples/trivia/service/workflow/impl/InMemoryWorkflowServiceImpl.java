package org.bj.examples.trivia.service.workflow.impl;

import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.WorkflowException;
import org.bj.examples.trivia.service.workflow.WorkflowService;
import org.springframework.stereotype.Service;

@Service
public class InMemoryWorkflowServiceImpl implements WorkflowService {
    private SlackUser currentHost = null;
    private boolean questionSubmitted = false;

    public void onGameStarted(final String channelId, final String userId) throws WorkflowException {
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

    public void onQuestionSubmission(final String channelId, final String userId) throws WorkflowException {
        if (userId == null) {
            return;
        }

        if (currentHost == null) {
            throw new WorkflowException("A game has not yet been started. If you'd like to start a game, try `/moviegame start`");
        } else {
            boolean isControllingUser = currentHost.getUserId().equals(userId);

            if (questionSubmitted) {
                throw new WorkflowException((isControllingUser ? "You have" : "<@" + currentHost.getUserId() + "> has") + " already asked a question.");
            } else if (!isControllingUser) {
                throw new WorkflowException("It's <@" + currentHost.getUserId() + ">'s turn to ask a question.");
            }
        }

        questionSubmitted = true;
    }

    public void onAnswerSubmission(final String channelId, final String userId) throws WorkflowException {
        if (userId == null) {
            return;
        }

        if (currentHost == null) {
            throw new WorkflowException("A game has not yet been started. If you'd like to start a game, try `/moviegame start`");
        } else if (currentHost.getUserId().equals(userId)) {
            throw new WorkflowException("You can't answer your own question!");
        } else if (!questionSubmitted) {
            throw new WorkflowException("A question has not yet been submitted. Please wait for <@" + currentHost.getUserId() + "> to ask a question.");
        }
    }

    public void onCorrectAnswer(final String channelId, final String userId) throws WorkflowException {
        if (userId == null) {
            return;
        }

        if (currentHost == null) {
            throw new WorkflowException("A game has not yet been started. If you'd like to start a game, try `/moviegame start`");
        } else if (!currentHost.getUserId().equals(userId)) {
            throw new WorkflowException("It's <@" + currentHost.getUserId() + ">'s question. Only he/she can mark an answer correct.");
        } else if (!questionSubmitted) {
            throw new WorkflowException("A question has not yet been submitted. Please ask a question before marking an answer correct.");
        }
    }

    public void onTurnChange(final String channelId, final String userId, final String newControllingUserId) throws WorkflowException {
        if (userId == null || newControllingUserId == null) {
            return;
        }

        if (currentHost == null) {
            throw new WorkflowException("A game has not yet been started. If you'd like to start a game, try `/moviegame start`");
        } else if (!currentHost.getUserId().equals(userId)) {
            throw new WorkflowException("It's <@" + currentHost.getUserId() + ">'s turn; only he/she can cede his/her turn.");
        }

        currentHost = new SlackUser(newControllingUserId, null);
        questionSubmitted = false;
    }
}
