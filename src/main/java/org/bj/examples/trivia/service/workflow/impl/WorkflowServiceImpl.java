package org.bj.examples.trivia.service.workflow.impl;

import org.bj.examples.trivia.dao.workflow.Workflow;
import org.bj.examples.trivia.dao.workflow.WorkflowDao;
import org.bj.examples.trivia.dao.workflow.WorkflowStage;
import org.bj.examples.trivia.exception.GameNotStartedException;
import org.bj.examples.trivia.exception.WorkflowException;
import org.bj.examples.trivia.service.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("production")
@Service
public class WorkflowServiceImpl implements WorkflowService {
    private final WorkflowDao workflowDao;

    @Autowired
    public WorkflowServiceImpl(final WorkflowDao workflowDao) {
        this.workflowDao = workflowDao;
    }

    public void onGameStarted(final String channelId, final String userId) throws WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow != null && workflow.getStage() != WorkflowStage.NOT_STARTED) {
            final String message = userId.equals(workflow.getControllingUserId()) ?
                    "You are already hosting!" :
                    "<@" + workflow.getControllingUserId() + "> is currently hosting.";

            throw new WorkflowException(message);
        }

        if (workflow == null) {
            workflow = new Workflow.Builder()
                    .channelId(channelId)
                    .build();
        }

        workflow.setStage(WorkflowStage.STARTED);
        workflow.setControllingUserId(userId);
        workflowDao.save(workflow);
    }

    public void onGameStopped(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null || workflow.getStage() == WorkflowStage.NOT_STARTED) {
            throw new GameNotStartedException();
        } else if (!userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("<@" + workflow.getControllingUserId() + "> is currently hosting.");
        }

        workflowDao.delete(workflow.getId());
    }

    public void onQuestionSubmission(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null || workflow.getStage() == WorkflowStage.NOT_STARTED) {
            throw new GameNotStartedException();
        } else {
            boolean isControllingUser = userId.equals(workflow.getControllingUserId());

            if (workflow.getStage() == WorkflowStage.QUESTION_ASKED) {
                throw new WorkflowException((isControllingUser ? "You have" : "<@" + workflow.getControllingUserId() + "> has") + " already asked a question.");
            } else if (!isControllingUser) {
                throw new WorkflowException("It's <@" + workflow.getControllingUserId() + ">'s turn to ask a question.");
            }
        }

        workflow.setStage(WorkflowStage.QUESTION_ASKED);
        workflowDao.save(workflow);
    }

    public void onAnswerSubmission(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null || workflow.getStage() == WorkflowStage.NOT_STARTED) {
            throw new GameNotStartedException();
        } else if (userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("You can't answer your own question!");
        } else if (workflow.getStage() != WorkflowStage.QUESTION_ASKED) {
            throw new WorkflowException("A question has not yet been submitted. Please wait for <@" + workflow.getControllingUserId() + "> to ask a question.");
        }
    }

    public void onCorrectAnswer(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null || workflow.getStage() == WorkflowStage.NOT_STARTED) {
            throw new GameNotStartedException();
        } else if (!userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("It's <@" + workflow.getControllingUserId() + ">'s turn; only he/she can mark an answer correct.");
        } else if (workflow.getStage() != WorkflowStage.QUESTION_ASKED) {
            throw new WorkflowException("A question has not yet been submitted. Please ask a question before marking an answer correct.");
        }
    }

    public void onTurnChange(final String channelId, final String userId, final String newControllingUserId)
            throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null || newControllingUserId == null) {
            return;
        }

        Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null || workflow.getStage() == WorkflowStage.NOT_STARTED) {
            throw new GameNotStartedException();
        } else if (!userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("It's <@" + workflow.getControllingUserId() + ">'s turn; only he/she can cede his/her turn.");
        }

        workflow.setControllingUserId(newControllingUserId);
        workflow.setStage(WorkflowStage.STARTED);
        workflowDao.save(workflow);
    }
}
