package org.bj.examples.trivia.message;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableSet;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageConfigTest {
    @Autowired
    public MessageConfig cut;

    @Test
    public void testMessagesLoadedCorrectly() {
        assertThat(cut.getGameStartMessages(), is(notNullValue()));
        assertThat(cut.getGameStartMessages(), hasSize(2));
        assertThat(cut.getGameStartMessages().get(0), is(equalTo("OK, <@%s>, please ask a question.")));
        assertThat(cut.getGameStartMessages().get(1), is(equalTo("Let the game begin! Start us off, <@%s>!")));

        assertThat(cut.getGameStopMessages(), is(notNullValue()));
        assertThat(cut.getGameStopMessages(), hasSize(1));
        assertThat(cut.getGameStopMessages().get(0), is(equalTo("The game has been stopped but scores have not been cleared. If you'd like to start a new game, try `%1$s start`.")));

        assertThat(cut.getPlayerAddedMessages(), is(notNullValue()));
        assertThat(cut.getPlayerAddedMessages(), hasSize(3));
        assertThat(cut.getPlayerAddedMessages().get(0), is(equalTo("<@%s> has joined the game!")));
        assertThat(cut.getPlayerAddedMessages().get(1), is(equalTo("Uh oh, here comes <@%s>!")));
        assertThat(cut.getPlayerAddedMessages().get(2), is(equalTo("We have a new arrival: <@%s>!")));

        assertThat(cut.getTurnPassedMessages(), is(notNullValue()));
        assertThat(cut.getTurnPassedMessages(), hasSize(1));
        assertThat(cut.getTurnPassedMessages().get(0), is(equalTo("<@%1$s> has decided to pass his/her turn to <@%2$s>.\n\nOK, <@%2$s>, it's your turn to ask a question!")));

        assertThat(cut.getQuestionSubmittedMessages(), is(notNullValue()));
        assertThat(cut.getQuestionSubmittedMessages(), hasSize(1));
        assertThat(cut.getQuestionSubmittedMessages().get(0), is(equalTo("<@%s> asked the following question:\n\n%s")));

        assertThat(cut.getAnswerSubmittedMessages(), is(notNullValue()));
        assertThat(cut.getAnswerSubmittedMessages(), hasSize(1));
        assertThat(cut.getAnswerSubmittedMessages().get(0), is(equalTo("<@%s> answers:")));

        assertThat(cut.getIncorrectAnswerMessages(), is(notNullValue()));
        assertThat(cut.getIncorrectAnswerMessages(), hasSize(1));
        assertThat(cut.getIncorrectAnswerMessages().get(0), is(equalTo("You couldn't be more wrong, <@%s>")));

        assertThat(cut.getNoCorrectanswerMessages(), is(notNullValue()));
        assertThat(cut.getNoCorrectanswerMessages(), hasSize(1));
        assertThat(cut.getNoCorrectanswerMessages(), everyItem(new AnswerTypeKeysMatcher()));
        assertThat(cut.getNoCorrectanswerMessages().get(0).get(AnswerType.WITH_ANSWER), is(equalTo("It looks like no one was able to answer that one! The correct answer was \"%1$s\".\n\n%2$s\n\nOK, <@%3$s>, let's try another one!")));
        assertThat(cut.getNoCorrectanswerMessages().get(0).get(AnswerType.WITHOUT_ANSWER), is(equalTo("It looks like no one was able to answer that one!\n\n%2$s\n\nOK, <@%3$s>, let's try another one!")));

        assertThat(cut.getCorrectAnswerMessages(), is(notNullValue()));
        assertThat(cut.getCorrectAnswerMessages(), everyItem(new AnswerTypeKeysMatcher()));
        assertThat(cut.getCorrectAnswerMessages(), hasSize(1));
        assertThat(cut.getCorrectAnswerMessages().get(0).get(AnswerType.WITH_ANSWER), is(equalTo("<@%3$s> is correct with \"%1$s\"!\n\n%2$s\n\nOK, <@%3$s>, you're up!")));
        assertThat(cut.getCorrectAnswerMessages().get(0).get(AnswerType.WITHOUT_ANSWER), is(equalTo("<@%3$s> is correct!\n\n%2$s\n\nOK, <@%3$s>, you're up!")));
    }

    @Test
    public void testMessagesFormatArgumentsCorrectly() {
        final String command = "/game";
        final String userId1 = "U12345";
        final String userId2 = "U12346";

        assertThat(String.format(cut.getGameStartMessages().get(0),  userId1), is(equalTo("OK, <@U12345>, please ask a question.")));
        assertThat(String.format(cut.getGameStartMessages().get(1),  userId1), is(equalTo("Let the game begin! Start us off, <@U12345>!")));

        assertThat(
                String.format(cut.getGameStopMessages().get(0), command, userId1),
                is(equalTo("The game has been stopped but scores have not been cleared. If you'd like to start a new game, try `/game start`."))
        );

        assertThat(String.format(cut.getPlayerAddedMessages().get(0),  userId1), is(equalTo("<@U12345> has joined the game!")));
        assertThat(String.format(cut.getPlayerAddedMessages().get(1),  userId1), is(equalTo("Uh oh, here comes <@U12345>!")));
        assertThat(String.format(cut.getPlayerAddedMessages().get(2),  userId1), is(equalTo("We have a new arrival: <@U12345>!")));

        assertThat(
                String.format(cut.getTurnPassedMessages().get(0), userId1, userId2),
                is(equalTo("<@U12345> has decided to pass his/her turn to <@U12346>.\n\nOK, <@U12346>, it's your turn to ask a question!"))
        );

        assertThat(String.format(cut.getQuestionSubmittedMessages().get(0),  userId1, "some question"), is(equalTo("<@U12345> asked the following question:\n\nsome question")));

        assertThat(String.format(cut.getAnswerSubmittedMessages().get(0),  userId1), is(equalTo("<@U12345> answers:")));

        assertThat(String.format(cut.getIncorrectAnswerMessages().get(0),  userId1), is(equalTo("You couldn't be more wrong, <@U12345>")));

        assertThat(
                String.format(cut.getNoCorrectanswerMessages().get(0).get(AnswerType.WITH_ANSWER), "some answer", "scores", userId1),
                is(equalTo("It looks like no one was able to answer that one! The correct answer was \"some answer\".\n\nscores\n\nOK, <@U12345>, let's try another one!"))
        );
        assertThat(
                String.format(cut.getNoCorrectanswerMessages().get(0).get(AnswerType.WITHOUT_ANSWER), null, "scores", userId1),
                is(equalTo("It looks like no one was able to answer that one!\n\nscores\n\nOK, <@U12345>, let's try another one!"))
        );

        assertThat(
                String.format(cut.getCorrectAnswerMessages().get(0).get(AnswerType.WITH_ANSWER), "some answer", "scores", userId1),
                is(equalTo("<@U12345> is correct with \"some answer\"!\n\nscores\n\nOK, <@U12345>, you're up!"))
        );
        assertThat(
                String.format(cut.getCorrectAnswerMessages().get(0).get(AnswerType.WITHOUT_ANSWER), null, "scores", userId1),
                is(equalTo("<@U12345> is correct!\n\nscores\n\nOK, <@U12345>, you're up!"))
        );
    }

    private static class AnswerTypeKeysMatcher extends BaseMatcher<Map<AnswerType, String>> {
        @Override
        public boolean matches(Object item) {
            if (!(item instanceof Map)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            Map<AnswerType, String> messagesByAnswerType = ((Map<AnswerType, String>)item);
            return messagesByAnswerType.keySet().equals(ImmutableSet.of(AnswerType.WITH_ANSWER, AnswerType.WITHOUT_ANSWER));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a map with all AnswerType keys");
        }

    }
}
