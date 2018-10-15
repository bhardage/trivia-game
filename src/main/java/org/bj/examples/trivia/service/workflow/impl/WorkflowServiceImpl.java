package org.bj.examples.trivia.service.workflow.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bj.examples.trivia.dao.workflow.Answer;
import org.bj.examples.trivia.dao.workflow.Workflow;
import org.bj.examples.trivia.dao.workflow.WorkflowDao;
import org.bj.examples.trivia.dao.workflow.WorkflowStage;
import org.bj.examples.trivia.dto.GameState;
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

    @Override
    public void onGameStarted(final String channelId, final String userId) throws WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow != null) {
            final String message = userId.equals(workflow.getControllingUserId()) ?
                    "You are already hosting!" :
                    "<@" + workflow.getControllingUserId() + "> is currently hosting.";

            throw new WorkflowException(message);
        }

        workflow = new Workflow.Builder()
                .channelId(channelId)
                .controllingUserId(userId)
                .question(null)
                .stage(WorkflowStage.STARTED)
                .build();
        workflowDao.save(workflow);
    }

    @Override
    public void onGameStopped(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null) {
            throw new GameNotStartedException();
        } else if (!userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("<@" + workflow.getControllingUserId() + "> is currently hosting.");
        }

        workflowDao.delete(workflow.getId());
    }

    @Override
    public void onQuestionSubmitted(final String channelId, final String userId, final String question) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null) {
            throw new GameNotStartedException();
        } else {
            boolean isControllingUser = userId.equals(workflow.getControllingUserId());

            if (workflow.getStage() == WorkflowStage.QUESTION_ASKED) {
                throw new WorkflowException((isControllingUser ? "You have" : "<@" + workflow.getControllingUserId() + "> has") + " already asked a question.");
            } else if (!isControllingUser) {
                throw new WorkflowException("It's <@" + workflow.getControllingUserId() + ">'s turn to ask a question.");
            }
        }

        workflow.setQuestion(question);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);
        workflowDao.save(workflow);
    }

    @Override
    public void onAnswerSubmitted(
            final String channelId,
            final String userId,
            final String username,
            final String answerText,
            final LocalDateTime createdDate
    ) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null) {
            throw new GameNotStartedException();
        } else if (userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("You can't answer your own question!");
        } else if (workflow.getStage() != WorkflowStage.QUESTION_ASKED) {
            throw new WorkflowException("A question has not yet been submitted. Please wait for <@" + workflow.getControllingUserId() + "> to ask a question.");
        }

        final Answer answer = new Answer.Builder()
                .userId(userId)
                .username(username)
                .text(answerText)
                .createdDate(createdDate)
                .build();
        workflow.getAnswers().add(answer);
        workflowDao.save(workflow);
    }

    @Override
    public void onCorrectAnswerSelected(final String channelId, final String userId) throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null) {
            return;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null) {
            throw new GameNotStartedException();
        } else if (!userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("It's <@" + workflow.getControllingUserId() + ">'s turn; only he/she can mark an answer correct.");
        } else if (workflow.getStage() != WorkflowStage.QUESTION_ASKED) {
            throw new WorkflowException("A question has not yet been submitted. Please ask a question before marking an answer correct.");
        }
    }

    @Override
    public void onTurnChanged(final String channelId, final String userId, final String newControllingUserId)
            throws GameNotStartedException, WorkflowException {
        if (channelId == null || userId == null || newControllingUserId == null) {
            return;
        }

        Workflow workflow = workflowDao.findByChannelId(channelId);

        if (workflow == null) {
            throw new GameNotStartedException();
        } else if (!userId.equals(workflow.getControllingUserId())) {
            throw new WorkflowException("It's <@" + workflow.getControllingUserId() + ">'s turn; only he/she can cede his/her turn.");
        }

        workflow.setControllingUserId(newControllingUserId);
        workflow.setQuestion(null);
        workflow.setAnswers(new ArrayList<>());
        workflow.setStage(WorkflowStage.STARTED);
        workflowDao.save(workflow);
    }

    @Override
    public GameState getCurrentGameState(final String channelId) {
        if (channelId == null) {
            return null;
        }

        final Workflow workflow = workflowDao.findByChannelId(channelId);
        final GameState gameState;

        if (workflow == null) {
            gameState = new GameState(null, null, null);
        } else if (workflow.getStage() == WorkflowStage.STARTED) {
            gameState = new GameState(workflow.getControllingUserId(), null, null);
        } else {
            final List<GameState.Answer> answers = workflow.getAnswers().stream()
                    .map(answer -> new GameState.Answer(answer.getUserId(), answer.getUsername(), answer.getText(), answer.getCreatedDate()))
                    .collect(Collectors.toList());
            gameState = new GameState(workflow.getControllingUserId(), workflow.getQuestion(), answers);
        }

        return gameState;
    }
}
