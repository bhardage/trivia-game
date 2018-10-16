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

import java.time.LocalDateTime;
import java.util.List;

import org.bj.examples.trivia.dao.workflow.Answer;
import org.bj.examples.trivia.dao.workflow.Workflow;
import org.bj.examples.trivia.dao.workflow.WorkflowDao;
import org.bj.examples.trivia.dao.workflow.WorkflowStage;
import org.bj.examples.trivia.exception.GameNotStartedException;
import org.bj.examples.trivia.exception.WorkflowException;
import org.bson.types.ObjectId;
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

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

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

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

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

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

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
        final ObjectId id = new ObjectId();
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow();
        workflow.setId(id);
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onGameStopped(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao).delete(id.toHexString());
    }
    //endregion

    //region onQuestionSubmitted
    @Test
    public void testOnQuestionSubmittedWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onQuestionSubmitted(null, "12345", "test question");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnQuestionSubmittedWithNullUserId() {
        Exception exception = null;

        try {
            cut.onQuestionSubmitted("12345", null, "test question");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnQuestionSubmittedWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String question = "test question";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onQuestionSubmitted(channelId, userId, question);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnQuestionSubmittedWithDifferentHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";
        final String question = "test question";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmitted(channelId, userId, question);
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
    public void testOnQuestionSubmittedWithDifferentHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";
        final String question = "test question";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.STARTED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmitted(channelId, userId, question);
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
    public void testOnQuestionSubmittedWithSameHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String question = "test question";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmitted(channelId, userId, question);
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
    public void testOnQuestionSubmittedWithSameHostAndNoQuestionAsked() {
        final ObjectId id = new ObjectId();
        final String channelId = "C12345";
        final String userId = "U6789";
        final String question = "test question";

        final Workflow workflow = new Workflow();
        workflow.setId(id);
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.STARTED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onQuestionSubmitted(channelId, userId, question);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);

        ArgumentCaptor<Workflow> workflowCaptor = ArgumentCaptor.forClass(Workflow.class);
        verify(workflowDao).save(workflowCaptor.capture());

        assertThat(workflowCaptor.getValue(), is(notNullValue()));
        assertThat(workflowCaptor.getValue().getId(), is(equalTo(id)));
        assertThat(workflowCaptor.getValue().getChannelId(), is(equalTo(channelId)));
        assertThat(workflowCaptor.getValue().getControllingUserId(), is(equalTo(userId)));
        assertThat(workflowCaptor.getValue().getQuestion(), is(equalTo(question)));
        assertThat(workflowCaptor.getValue().getStage(), is(equalTo(WorkflowStage.QUESTION_ASKED)));
    }
    //endregion

    //region onAnswerSubmitted
    @Test
    public void testOnAnswerSubmittedWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onAnswerSubmitted(null, "12345", null, null, null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnAnswerSubmittedWithNullUserId() {
        Exception exception = null;

        try {
            cut.onAnswerSubmitted("12345", null, null, null, null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnAnswerSubmittedWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onAnswerSubmitted(channelId, userId, null, null, null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnAnswerSubmittedWithSameHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.STARTED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmitted(channelId, userId, null, null, null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("You can't answer your own question!")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnAnswerSubmittedWithSameHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmitted(channelId, userId, null, null, null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("You can't answer your own question!")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnAnswerSubmittedWithDifferentHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.STARTED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmitted(channelId, userId, null, null, null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("A question has not yet been submitted. Please wait for <@" + controllingUserId + "> to ask a question.")));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnAnswerSubmittedWithDifferentHostAndQuestionAsked() {
        final ObjectId id = new ObjectId();
        final String channelId = "C12345";
        final String userId = "U6789";
        final String username = "myusername";
        final String answerText = "answer test";
        final LocalDateTime answerTime = LocalDateTime.now();
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow();
        workflow.setId(id);
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onAnswerSubmitted(channelId, userId, username, answerText, answerTime);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);

        ArgumentCaptor<Workflow> workflowCaptor = ArgumentCaptor.forClass(Workflow.class);
        verify(workflowDao).save(workflowCaptor.capture());

        assertThat(workflowCaptor.getValue(), is(notNullValue()));
        assertThat(workflowCaptor.getValue().getId(), is(equalTo(id)));
        assertThat(workflowCaptor.getValue().getChannelId(), is(equalTo(channelId)));
        assertThat(workflowCaptor.getValue().getControllingUserId(), is(equalTo(controllingUserId)));
        assertThat(workflowCaptor.getValue().getStage(), is(equalTo(WorkflowStage.QUESTION_ASKED)));

        final List<Answer> answers = workflowCaptor.getValue().getAnswers();
        assertThat(answers, is(notNullValue()));
        assertThat(answers.size(), is(equalTo(1)));
        assertThat(answers.get(0).getUserId(), is(equalTo(userId)));
        assertThat(answers.get(0).getUsername(), is(equalTo(username)));
        assertThat(answers.get(0).getText(), is(equalTo(answerText)));
        assertThat(answers.get(0).getCreatedDate(), is(equalTo(answerTime)));
    }
    //endregion

    //region onCorrectAnswerSelected
    @Test
    public void testOnCorrectAnswerSelectedWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onCorrectAnswerSelected(null, "12345");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnCorrectAnswerSelectedWithNullUserId() {
        Exception exception = null;

        try {
            cut.onCorrectAnswerSelected("12345", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnCorrectAnswerSelectedWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onCorrectAnswerSelected(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerSelectedWithDifferentHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.STARTED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswerSelected(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("It's <@" + controllingUserId + ">'s turn; only he/she can mark an answer correct.")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerSelectedWithDifferentHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswerSelected(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("It's <@" + controllingUserId + ">'s turn; only he/she can mark an answer correct.")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerSelectedWithSameHostAndNoQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.STARTED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswerSelected(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("A question has not yet been submitted. Please ask a question before marking an answer correct.")));

        verify(workflowDao).findByChannelId(channelId);
    }

    @Test
    public void testOnCorrectAnswerSelectedWithSameHostAndQuestionAsked() {
        final String channelId = "C12345";
        final String userId = "U6789";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onCorrectAnswerSelected(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);
    }
    //endregion

    //region onTurnChanged
    @Test
    public void testOnTurnChangedWithNullChannelId() {
        Exception exception = null;

        try {
            cut.onTurnChanged(null, "12345", "6789");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnTurnChangedWithNullUserId() {
        Exception exception = null;

        try {
            cut.onTurnChanged("12345", null, "6789");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnTurnChangedWithNullControllingUserId() {
        Exception exception = null;

        try {
            cut.onTurnChanged("12345", "6789", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verifyZeroInteractions(workflowDao);
    }

    @Test
    public void testOnTurnChangedWithNoExistingWorkflow() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String newControllingUserId = "U1532";

        given(workflowDao.findByChannelId(anyString())).willReturn(null);

        Exception exception = null;

        try {
            cut.onTurnChanged(channelId, userId, newControllingUserId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(GameNotStartedException.class)));

        verify(workflowDao).findByChannelId(channelId);
        verify(workflowDao, never()).save(any());
    }

    @Test
    public void testOnTurnChangedWithDifferentHost() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String newControllingUserId = "U1532";
        final String controllingUserId = "U1346";

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId());
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(controllingUserId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onTurnChanged(channelId, userId, newControllingUserId);
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
    public void testOnTurnChangedWithSameHost() {
        final ObjectId id = new ObjectId();
        final String channelId = "C12345";
        final String userId = "U6789";
        final String newControllingUserId = "U1532";

        final Workflow workflow = new Workflow();
        workflow.setId(id);
        workflow.setChannelId(channelId);
        workflow.setControllingUserId(userId);
        workflow.setStage(WorkflowStage.QUESTION_ASKED);

        given(workflowDao.findByChannelId(anyString())).willReturn(workflow);

        Exception exception = null;

        try {
            cut.onTurnChanged(channelId, userId, newControllingUserId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));

        verify(workflowDao).findByChannelId(channelId);

        ArgumentCaptor<Workflow> workflowCaptor = ArgumentCaptor.forClass(Workflow.class);
        verify(workflowDao).save(workflowCaptor.capture());

        assertThat(workflowCaptor.getValue(), is(notNullValue()));
        assertThat(workflowCaptor.getValue().getId(), is(equalTo(id)));
        assertThat(workflowCaptor.getValue().getChannelId(), is(equalTo(channelId)));
        assertThat(workflowCaptor.getValue().getControllingUserId(), is(equalTo(newControllingUserId)));
        assertThat(workflowCaptor.getValue().getStage(), is(equalTo(WorkflowStage.STARTED)));
    }
    //endregion
}
