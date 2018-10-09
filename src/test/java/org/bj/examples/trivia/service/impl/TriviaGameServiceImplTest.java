package org.bj.examples.trivia.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Map;

import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.service.ScoreService;
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
    public ScoreService scoreService;

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
