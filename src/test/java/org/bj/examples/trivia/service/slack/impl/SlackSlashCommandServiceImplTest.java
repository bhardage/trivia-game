package org.bj.examples.trivia.service.slack.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.dto.SlackResponseType;
import org.bj.examples.trivia.service.game.TriviaGameService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SlackSlashCommandServiceImplTest {
    @InjectMocks
    public SlackSlashCommandServiceImpl cut;

    @Mock
    private TriviaGameService triviaGameService;

    @Test
    public void testStartCommand() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("  start  ");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.start(any(SlackRequestDoc.class))).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).start(requestDoc);
    }

    @Test
    public void testStopCommand() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("  stop  ");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.stop(any(SlackRequestDoc.class))).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).stop(requestDoc);
    }

    @Test
    public void testQuestionCommandWithTooFewArguments() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("question");

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(notNullValue()));
        assertThat(result.getResponseType(), is(SlackResponseType.EPHEMERAL));
        assertThat(result.getText(), is("To submit a question, use `/moviegame question <QUESTION_TEXT>`.\n\nFor example, `/moviegame question In what year did WWII officially begin?`"));
        assertThat(result.getAttachments(), is(nullValue()));

        verify(triviaGameService, never()).submitQuestion(any(), any());
    }

    @Test
    public void testQuestionCommand() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("  question   What    does ATM stand for?   ");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.submitQuestion(any(), any())).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).submitQuestion(requestDoc, "What    does ATM stand for?");
    }

    @Test
    public void testAnswerCommandWithTooFewArguments() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("answer");

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(notNullValue()));
        assertThat(result.getResponseType(), is(SlackResponseType.EPHEMERAL));
        assertThat(result.getText(), is("To submit an answer, use `/moviegame answer <ANSWER_TEXT>`.\n\nFor example, `/moviegame answer Blue skies`"));
        assertThat(result.getAttachments(), is(nullValue()));

        verify(triviaGameService, never()).submitAnswer(any(), any());
    }

    @Test
    public void testAnswerCommand() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("  answer   I    do not    know   ");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.submitAnswer(any(), any())).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).submitAnswer(requestDoc, "I    do not    know");
    }

    @Test
    public void testMarkCorrectCommandWithTooFewArguments() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("correct");

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(notNullValue()));
        assertThat(result.getResponseType(), is(SlackResponseType.EPHEMERAL));
        assertThat(result.getText(), is("To mark an answer correct, use `/moviegame correct <USERNAME>`.\n"
                + "Optional: To include the correct answer, use `/moviegame correct <USERNAME> <CORRECT_ANSWER>`.\n\n"
                + "For example, `/moviegame correct @jsmith Chris Farley`"));
        assertThat(result.getAttachments(), is(nullValue()));

        verify(triviaGameService, never()).markAnswerCorrect(any(), any(), any());
    }

    @Test
    public void testMarkCorrectCommandWithNoAnswer() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("correct <@12345>");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.markAnswerCorrect(any(), any(), any())).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).markAnswerCorrect(requestDoc, "<@12345>", null);
    }

    @Test
    public void testMarkCorrectCommandWithAnswer() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("correct <@12345>   I    do not    know");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.markAnswerCorrect(any(), any(), any())).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).markAnswerCorrect(requestDoc, "<@12345>", "I    do not    know");
    }

    @Test
    public void testGetScoresCommand() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("  scores  ");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.getScores(any(SlackRequestDoc.class))).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).getScores(requestDoc);
    }

    @Test
    public void testResetScoresCommand() {
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setText("  reset  ");

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        given(triviaGameService.resetScores(any(SlackRequestDoc.class))).willReturn(responseDoc);

        final SlackResponseDoc result = cut.processSlashCommand(requestDoc);

        assertThat(result, is(responseDoc));

        verify(triviaGameService).resetScores(requestDoc);
    }
}
