package org.bj.examples.trivia.service.game.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Map;

import org.bj.examples.trivia.dto.GameState;
import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.dto.SlackResponseType;
import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.service.score.ScoreService;
import org.bj.examples.trivia.service.workflow.WorkflowService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TriviaGameServiceImplTest {
    @InjectMocks
    public TriviaGameServiceImpl cut;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private ScoreService scoreService;

    //region getStatus
    @Test
    public void testGetStatusWithNullGameState() {
        final String channelId = "channel";
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);
        requestDoc.setCommand("/command");

        given(workflowService.getCurrentGameState(anyString())).willReturn(null);

        final SlackResponseDoc responseDoc = cut.getStatus(requestDoc);

        assertThat(responseDoc, is(notNullValue()));
        assertThat(responseDoc.getResponseType(), is(equalTo(SlackResponseType.EPHEMERAL)));
        assertThat(responseDoc.getText(), is(equalTo("A game has not yet been started. If you'd like to start a game, try `/command start`")));
    }

    @Test
    public void testGetStatusWithNullHostInGameState() {
        final String channelId = "channel";
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);
        requestDoc.setCommand("/command");

        final GameState gameState = new GameState(null, null);

        given(workflowService.getCurrentGameState(anyString())).willReturn(gameState);

        final SlackResponseDoc responseDoc = cut.getStatus(requestDoc);

        assertThat(responseDoc, is(notNullValue()));
        assertThat(responseDoc.getResponseType(), is(equalTo(SlackResponseType.EPHEMERAL)));
        assertThat(responseDoc.getText(), is(equalTo("A game has not yet been started. If you'd like to start a game, try `/command start`")));
    }

    @Test
    public void testGetStatusWithSameHostAndNoQuestionInGameState() {
        final String channelId = "channel";
        final String userId = "U12345";

        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);
        requestDoc.setCommand("/command");
        requestDoc.setUserId(userId);

        final GameState gameState = new GameState(userId, null);

        given(workflowService.getCurrentGameState(anyString())).willReturn(gameState);

        final SlackResponseDoc responseDoc = cut.getStatus(requestDoc);

        assertThat(responseDoc, is(notNullValue()));
        assertThat(responseDoc.getResponseType(), is(equalTo(SlackResponseType.EPHEMERAL)));
        assertThat(responseDoc.getText(), is(equalTo("It's your turn and no question has been asked yet.")));
    }

    @Test
    public void testGetStatusWithDifferentHostAndNoQuestionInGameState() {
        final String channelId = "channel";
        final String userId = "U12345";
        final String controllingUserId = "U6789";

        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);
        requestDoc.setCommand("/command");
        requestDoc.setUserId(userId);

        final GameState gameState = new GameState(controllingUserId, null);

        given(workflowService.getCurrentGameState(anyString())).willReturn(gameState);

        final SlackResponseDoc responseDoc = cut.getStatus(requestDoc);

        assertThat(responseDoc, is(notNullValue()));
        assertThat(responseDoc.getResponseType(), is(equalTo(SlackResponseType.EPHEMERAL)));
        assertThat(responseDoc.getText(), is(equalTo("It's <@" + controllingUserId + ">'s turn and no question has been asked yet.")));
    }

    @Test
    public void testGetStatusWithSameHostAndQuestionInGameState() {
        final String channelId = "channel";
        final String userId = "U12345";
        final String question = "some question?";

        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);
        requestDoc.setCommand("/command");
        requestDoc.setUserId(userId);

        final GameState gameState = new GameState(userId, question);

        given(workflowService.getCurrentGameState(anyString())).willReturn(gameState);

        final SlackResponseDoc responseDoc = cut.getStatus(requestDoc);

        assertThat(responseDoc, is(notNullValue()));
        assertThat(responseDoc.getResponseType(), is(equalTo(SlackResponseType.EPHEMERAL)));
        assertThat(responseDoc.getText(), is(equalTo("It's your turn and you have asked the following question:\n\nsome question?")));
    }

    @Test
    public void testGetStatusWithDifferentHostAndQuestionInGameState() {
        final String channelId = "channel";
        final String userId = "U12345";
        final String controllingUserId = "U6789";
        final String question = "some question?";

        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);
        requestDoc.setCommand("/command");
        requestDoc.setUserId(userId);

        final GameState gameState = new GameState(controllingUserId, question);

        given(workflowService.getCurrentGameState(anyString())).willReturn(gameState);

        final SlackResponseDoc responseDoc = cut.getStatus(requestDoc);

        assertThat(responseDoc, is(notNullValue()));
        assertThat(responseDoc.getResponseType(), is(equalTo(SlackResponseType.EPHEMERAL)));
        assertThat(responseDoc.getText(), is(equalTo("It's <@" + controllingUserId + ">'s turn and he/she has asked the following question:\n\nsome question?")));
    }
    //endregion

    @Test
    public void testGetScoresFormatsAndSortsCorrectly() {
        final Map<SlackUser, Long> scoresByUser = ImmutableMap.of(
                new SlackUser("1234", "test1"), 1L,
                new SlackUser("1235", "longertest2"), 103L,
                new SlackUser("1236", "unmanageablylongertest3"), 12L
        );
        final String channelId = "channel";
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);

        given(scoreService.getAllScoresByUser(channelId)).willReturn(scoresByUser);

        final SlackResponseDoc responseDoc = cut.getScores(requestDoc);

        /*
         * ```Scores:
         *
         * @longertest2:             103
         * @test1:                     1
         * @unmanageablylongertest3:  12```
         */
        assertThat(responseDoc.getText(), is("```Scores:\n\n@longertest2:             103\n@test1:                     1\n@unmanageablylongertest3:  12```"));
    }

    @Test
    public void testGetScoresWithNoUsers() {
        final String channelId = "channel";
        final SlackRequestDoc requestDoc = new SlackRequestDoc();
        requestDoc.setChannelId(channelId);

        given(scoreService.getAllScoresByUser(channelId)).willReturn(ImmutableMap.of());

        final SlackResponseDoc responseDoc = cut.getScores(requestDoc);

        /*
         * ```Scores:
         *
         * No scores yet...```
         */
        assertThat(responseDoc.getText(), is("```Scores:\n\nNo scores yet...```"));
    }
}
