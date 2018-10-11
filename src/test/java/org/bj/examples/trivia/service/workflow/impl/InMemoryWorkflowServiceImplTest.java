package org.bj.examples.trivia.service.workflow.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.WorkflowException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemoryWorkflowServiceImplTest {
    @InjectMocks
    public InMemoryWorkflowServiceImpl cut;

    //region onGameStarted
    @Test
    public void testOnGameStartedWithNullChannelId() {
        final String userId = "U12345";

        setCurrentHost(null);
        setQuestion(null);

        Exception exception = null;

        try {
            cut.onGameStarted(null, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));
        assertThat(getCurrentHost(), is(equalTo(new SlackUser(userId, null))));
        assertThat(getQuestion(), is(nullValue()));
    }

    @Test
    public void testOnGameStartedWithNullUserId() {
        setCurrentHost(null);
        setQuestion(null);

        Exception exception = null;

        try {
            cut.onGameStarted("12345", null);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));
        assertThat(getCurrentHost(), is(nullValue()));
        assertThat(getQuestion(), is(nullValue()));
    }

    @Test
    public void testOnGameStartedWithGameAlreadyStartedAndCurrentUserAsHost() {
        final String channelId = "C12345";
        final String userId = "U6789";

        setCurrentHost(new SlackUser(userId, null));
        setQuestion(null);

        Exception exception = null;

        try {
            cut.onGameStarted(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("You are already hosting!")));
    }

    @Test
    public void testOnGameStartedWithGameAlreadyStartedAndDifferentHost() {
        final String channelId = "C12345";
        final String userId = "U6789";
        final String controllingUserId = "U1346";

        setCurrentHost(new SlackUser(controllingUserId, null));
        setQuestion(null);

        Exception exception = null;

        try {
            cut.onGameStarted(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        assertThat(exception, is(instanceOf(WorkflowException.class)));
        assertThat(exception.getMessage(), is(equalTo("<@" + controllingUserId + "> is currently hosting.")));
    }

    @Test
    public void testOnGameStartedWithGameNotStarted() {
        final String channelId = "C12345";
        final String userId = "U6789";

        setCurrentHost(null);
        setQuestion(null);

        Exception exception = null;

        try {
            cut.onGameStarted(channelId, userId);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(nullValue()));
        assertThat(getCurrentHost(), is(equalTo(new SlackUser(userId, null))));
        assertThat(getQuestion(), is(nullValue()));
    }
    //endregion

    private SlackUser getCurrentHost() {
        final Field currentHostField = ReflectionUtils.findField(InMemoryWorkflowServiceImpl.class, "currentHost");
        ReflectionUtils.makeAccessible(currentHostField);

        return (SlackUser)ReflectionUtils.getField(currentHostField, cut);
    }

    private void setCurrentHost(final SlackUser currentHost) {
        final Field currentHostField = ReflectionUtils.findField(InMemoryWorkflowServiceImpl.class, "currentHost");
        ReflectionUtils.makeAccessible(currentHostField);

        ReflectionUtils.setField(currentHostField, cut, currentHost);
    }

    private String getQuestion() {
        final Field questionField = ReflectionUtils.findField(InMemoryWorkflowServiceImpl.class, "question");
        ReflectionUtils.makeAccessible(questionField);

        return ((String)ReflectionUtils.getField(questionField, cut));
    }

    private void setQuestion(final String question) {
        final Field questionField = ReflectionUtils.findField(InMemoryWorkflowServiceImpl.class, "question");
        ReflectionUtils.makeAccessible(questionField);

        ReflectionUtils.setField(questionField, cut, question);
    }
}
