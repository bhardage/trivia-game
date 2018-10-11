package org.bj.examples.trivia.service.workflow.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.bj.examples.trivia.dao.workflow.Workflow;
import org.bj.examples.trivia.dao.workflow.WorkflowDao;
import org.bj.examples.trivia.dao.workflow.WorkflowStage;
import org.bj.examples.trivia.exception.GameNotStartedException;
import org.bj.examples.trivia.exception.WorkflowException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class WorkflowServiceImplTest {
    @InjectMocks
    public WorkflowServiceImpl cut;

    @Mock
    private WorkflowDao workflowDao;

    //region onGameStarted
    @Test
    public void testOnGameStartedWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onGameStarted(null, "12345");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnGameStartedWithNullUserId() {
        Exception exception = null;

        try {
            cut.onGameStarted("12345", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnGameStartedWithGameAlreadyStartedAndCurrentUserAsHost() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onGameStarted(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("You are already hosting!")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnGameStartedWithGameAlreadyStartedAndDifferentHost() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onGameStarted(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("<@" + controllingUserId + "> is currently hosting.")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnGameStartedWithGameNotStarted() {
        final String channelId = "C12345";
        final String userId = "U6789";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onGameStarted(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);

        ArgumentCaptor<Workflow> workflowCaptor = ArgumentCaptor.forClass(Workflow.class);
        verify(workflowDao).save(workflowCaptor.capture());

        assertThat(workflowCaptor.getValue(), is(notNullValue()));
        assertThat(workflowCaptor.getValue().getChannelId(), is(equalTo(channelId)));
        assertThat(workflowCaptor.getValue().getControllingUserId(), is(equalTo(userId)));
        assertThat(workflowCaptor.getValue().getStage(), is(equalTo(WorkflowStage.STARTED)));
    }
    //endregion

    //region onGameStopped
    @Test
    public void testOnGameStoppedWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onGameStopped(null, "12345");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnGameStoppedWithNullUserId() {
        Exception exception = null;

        try {
            cut.onGameStopped("12345", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnGameStoppedWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onGameStopped(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).delete(any());
    }

    @Test
    public void testOnGameStoppedWithExistingWorkflowAndDifferentHost() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onGameStopped(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("<@" + controllingUserId + "> is currently hosting.")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).delete(any());
    }

    @Test
    public void testOnGameStoppedWithExistingWorkflowAndSameHost() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onGameStopped(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao).delete(1L);
    }
    //endregion

    //region onQuestionSubmission
    @Test
    public void testOnQuestionSubmissionWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onQuestionSubmission(null, "12345");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnQuestionSubmissionWithNullUserId() {
        Exception exception = null;

        try {
            cut.onQuestionSubmission("12345", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnQuestionSubmissionWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onQuestionSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnQuestionSubmissionWithDifferentHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("<@" + controllingUserId + "> has already asked a question.")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnQuestionSubmissionWithDifferentHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.STARTED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("It's <@" + controllingUserId + ">'s turn to ask a question.")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnQuestionSubmissionWithSameHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("You have already asked a question.")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnQuestionSubmissionWithSameHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.STARTED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);

        ArgumentCaptor<Workflow> workflowCaptor = ArgumentCaptor.forClass(Workflow.class);
        verify(workflowDao).save(workflowCaptor.capture());

        assertThat(workflowCaptor.getValue(), is(notNullValue()));
        assertThat(workflowCaptor.getValue().getId(), is(equalTo(1L)));
        assertThat(workflowCaptor.getValue().getChannelId(), is(equalTo(channelId)));
        assertThat(workflowCaptor.getValue().getControllingUserId(), is(equalTo(userId)));
        assertThat(workflowCaptor.getValue().getStage(), is(equalTo(WorkflowStage.QUESTION_ASKED)));
    }
    //endregion

    //region onAnswerSubmission
    @Test
    public void testOnAnswerSubmissionWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onAnswerSubmission(null, "12345");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnAnswerSubmissionWithNullUserId() {
        Exception exception = null;

        try {
            cut.onAnswerSubmission("12345", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnAnswerSubmissionWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onAnswerSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnAnswerSubmissionWithSameHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.STARTED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("You can't answer your own question!")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnAnswerSubmissionWithSameHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("You can't answer your own question!")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnAnswerSubmissionWithDifferentHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.STARTED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("A question has not yet been submitted. Please wait for <@" + controllingUserId + "> to ask a question.")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnAnswerSubmissionWithDifferentHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmission(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);
    }
    //endregion

    //region onCorrectAnswer
    @Test
    public void testOnCorrectAnswerWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onCorrectAnswer(null, "12345");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnCorrectAnswerWithNullUserId() {
        Exception exception = null;

        try {
            cut.onCorrectAnswer("12345", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnCorrectAnswerWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onCorrectAnswer(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerWithDifferentHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.STARTED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswer(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("It's <@" + controllingUserId + ">'s turn; only he/she can mark an answer correct.")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerWithDifferentHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswer(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("It's <@" + controllingUserId + ">'s turn; only he/she can mark an answer correct.")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerWithSameHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.STARTED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswer(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("A question has not yet been submitted. Please ask a question before marking an answer correct.")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerWithSameHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswer(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);
    }
    //endregion

    //region onTurnChange
    @Test
    public void testOnTurnChangeWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onTurnChange(null, "12345", "6789");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnTurnChangeWithNullUserId() {
        Exception exception = null;

        try {
            cut.onTurnChange("12345", null, "6789");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnTurnChangeWithNullControllingUserId() {
        Exception exception = null;

        try {
            cut.onTurnChange("12345", "6789", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnTurnChangeWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String newControllingUserId = "U1532";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onTurnChange(channelId, userId, newControllingUserId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnTurnChangeWithDifferentHost() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String newControllingUserId = "U1532";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(controllingUserId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onTurnChange(channelId, userId, newControllingUserId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("It's <@" + controllingUserId + ">'s turn; only he/she can cede his/her turn.")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnTurnChangeWithSameHost() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String newControllingUserId = "U1532";

        final Workflow workflow = new Workflow.Builder()
                .id(1L)
                .channelId(channelId)
                .controllingUserId(userId)
                .stage(WorkflowStage.QUESTION_ASKED)
                .build();

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onTurnChange(channelId, userId, newControllingUserId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);

        ArgumentCaptor<Workflow> workflowCaptor = ArgumentCaptor.forClass(Workflow.class);
        verify(workflowDao).save(workflowCaptor.capture());

        assertThat(workflowCaptor.getValue(), is(notNullValue()));
        assertThat(workflowCaptor.getValue().getId(), is(equalTo(1L)));
        assertThat(workflowCaptor.getValue().getChannelId(), is(equalTo(channelId)));
        assertThat(workflowCaptor.getValue().getControllingUserId(), is(equalTo(newControllingUserId)));
        assertThat(workflowCaptor.getValue().getStage(), is(equalTo(WorkflowStage.STARTED)));
    }
    //endregion
}
