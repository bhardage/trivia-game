package org.bj.examples.trivia.message;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageConfigTest {
    @Autowired
    public MessageConfig cut;

    @Test
    public void testMessagesLoadedCorrectly() {
        assertThat(cut.getGameStartMessages(), is(notNullValue()));
        assertThat(cut.getGameStartMessages(), hasSize(3));
        assertThat(cut.getGameStartMessages().get(0), is(equalTo("OK, <@%s>, please ask a question.")));
        assertThat(cut.getGameStartMessages().get(1), is(equalTo("Let the game begin! Start us off, <@%s>!")));
        assertThat(cut.getGameStartMessages().get(2), is(equalTo("Let's get ready to rumble, <@%s>!!!")));

        assertThat(cut.getGameStopMessages(), is(notNullValue()));
        assertThat(cut.getGameStopMessages(), hasSize(2));
        assertThat(cut.getGameStopMessages().get(0), is(equalTo("The game has been stopped but scores have not been cleared. If you'd like to start a new game, try `%1$s start`.")));
        assertThat(cut.getGameStopMessages().get(1), is(equalTo("Game trivia system shutting down in 3...2...1...done! If you'd like to start a new game, try `%1$s start`.")));

        assertThat(cut.getPlayerAddedMessages(), is(notNullValue()));
        assertThat(cut.getPlayerAddedMessages(), hasSize(5));
        assertThat(cut.getPlayerAddedMessages().get(0), is(equalTo("<@%s> has joined the game!")));
        assertThat(cut.getPlayerAddedMessages().get(1), is(equalTo("Uh oh, here comes <@%s>!")));
        assertThat(cut.getPlayerAddedMessages().get(2), is(equalTo("We have a new arrival: <@%s>!")));
        assertThat(cut.getPlayerAddedMessages().get(3), is(equalTo("Welcome home, <@%s>...")));
        assertThat(cut.getPlayerAddedMessages().get(4), is(equalTo("Hidy ho there <@%s>!")));

        assertThat(cut.getTurnPassedMessages(), is(notNullValue()));
        assertThat(cut.getTurnPassedMessages(), hasSize(2));
        assertThat(cut.getTurnPassedMessages().get(0), is(equalTo("<@%1$s> has decided to pass his/her turn to <@%2$s>.\n\nOK, <@%2$s>, it's your turn to ask a question!")));
        assertThat(cut.getTurnPassedMessages().get(1), is(equalTo("<@%1$s> has decided to pass the ball to <@%2$s>.\n\nOK, <@%2$s>, you're up!")));

        assertThat(cut.getQuestionSubmittedMessages(), is(notNullValue()));
        assertThat(cut.getQuestionSubmittedMessages(), hasSize(3));
        assertThat(cut.getQuestionSubmittedMessages().get(0), is(equalTo("<@%s> asked the following question:\n\n%s")));
        assertThat(cut.getQuestionSubmittedMessages().get(1), is(equalTo("<@%s> asked this brain buster:\n\n%s")));
        assertThat(cut.getQuestionSubmittedMessages().get(2), is(equalTo("The following question has been asked by <@%s>:\n\n%s")));

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
        assertThat(cut.getCorrectAnswerMessages(), hasSize(1));
        assertThat(cut.getCorrectAnswerMessages(), everyItem(new AnswerTypeKeysMatcher()));
        assertThat(cut.getCorrectAnswerMessages().get(0).get(AnswerType.WITH_ANSWER), is(equalTo("<@%3$s> is correct with \"%1$s\"!\n\n%2$s\n\nOK, <@%3$s>, you're up!")));
        assertThat(cut.getCorrectAnswerMessages().get(0).get(AnswerType.WITHOUT_ANSWER), is(equalTo("<@%3$s> is correct!\n\n%2$s\n\nOK, <@%3$s>, you're up!")));
    }

    @Test
    public void testMessagesFormatArgumentsCorrectly() {
        final String command = "/game";
        final String userId1 = "U12345";
        final String userId2 = "U12346";

        assertThat(cut.getGameStartMessages(), is(notNullValue()));
        final List<String> formattedGameStartMessages = cut.getGameStartMessages().stream()
                .map(message -> String.format(message, userId1))
                .collect(Collectors.toList());
        assertThat(formattedGameStartMessages, is(equalTo(ImmutableList.of(
                "OK, <@U12345>, please ask a question.",
                "Let the game begin! Start us off, <@U12345>!",
                "Let's get ready to rumble, <@U12345>!!!"
        ))));

        assertThat(cut.getGameStopMessages(), is(notNullValue()));
        final List<String> formattedGameStopMessages = cut.getGameStopMessages().stream()
                .map(message -> String.format(message, command, userId1))
                .collect(Collectors.toList());
        assertThat(formattedGameStopMessages, is(equalTo(ImmutableList.of(
                "The game has been stopped but scores have not been cleared. If you'd like to start a new game, try `/game start`.",
                "Game trivia system shutting down in 3...2...1...done! If you'd like to start a new game, try `/game start`."
        ))));

        assertThat(cut.getPlayerAddedMessages(), is(notNullValue()));
        final List<String> formattedPlayerAddedMessages = cut.getPlayerAddedMessages().stream()
                .map(message -> String.format(message, userId1))
                .collect(Collectors.toList());
        assertThat(formattedPlayerAddedMessages, is(equalTo(ImmutableList.of(
                "<@U12345> has joined the game!",
                "Uh oh, here comes <@U12345>!",
                "We have a new arrival: <@U12345>!",
                "Welcome home, <@U12345>...",
                "Hidy ho there <@U12345>!"
        ))));

        assertThat(cut.getTurnPassedMessages(), is(notNullValue()));
        final List<String> formattedTurnPassedMessages = cut.getTurnPassedMessages().stream()
                .map(message -> String.format(message, userId1, userId2))
                .collect(Collectors.toList());
        assertThat(formattedTurnPassedMessages, is(equalTo(ImmutableList.of(
                "<@U12345> has decided to pass his/her turn to <@U12346>.\n\nOK, <@U12346>, it's your turn to ask a question!",
                "<@U12345> has decided to pass the ball to <@U12346>.\n\nOK, <@U12346>, you're up!"
        ))));

        assertThat(cut.getQuestionSubmittedMessages(), is(notNullValue()));
        final List<String> formattedQuestionSubmittedMessages = cut.getQuestionSubmittedMessages().stream()
                .map(message -> String.format(message, userId1, "some question"))
                .collect(Collectors.toList());
        assertThat(formattedQuestionSubmittedMessages, is(equalTo(ImmutableList.of(
                "<@U12345> asked the following question:\n\nsome question",
                "<@U12345> asked this brain buster:\n\nsome question",
                "The following question has been asked by <@U12345>:\n\nsome question"
        ))));

        assertThat(cut.getAnswerSubmittedMessages(), is(notNullValue()));
        final List<String> formattedAnswerSubmittedMessages = cut.getAnswerSubmittedMessages().stream()
                .map(message -> String.format(message, userId1))
                .collect(Collectors.toList());
        assertThat(formattedAnswerSubmittedMessages, is(equalTo(ImmutableList.of(
                "<@U12345> answers:"
        ))));

        assertThat(cut.getIncorrectAnswerMessages(), is(notNullValue()));
        final List<String> formattedIncorrectAnswerMessages = cut.getIncorrectAnswerMessages().stream()
                .map(message -> String.format(message, userId1))
                .collect(Collectors.toList());
        assertThat(formattedIncorrectAnswerMessages, is(equalTo(ImmutableList.of(
                "You couldn't be more wrong, <@U12345>"
        ))));

        assertThat(cut.getNoCorrectanswerMessages(), is(notNullValue()));
        final List<String> formattedNoCorrectAnswerWithAnswerMessages = cut.getNoCorrectanswerMessages().stream()
                .map(messagesByAnswerType -> messagesByAnswerType.get(AnswerType.WITH_ANSWER))
                .map(message -> String.format(message, "some answer", "scores", userId1))
                .collect(Collectors.toList());
        final List<String> formattedNoCorrectAnswerWithoutAnswerMessages = cut.getNoCorrectanswerMessages().stream()
                .map(messagesByAnswerType -> messagesByAnswerType.get(AnswerType.WITHOUT_ANSWER))
                .map(message -> String.format(message, null, "scores", userId1))
                .collect(Collectors.toList());
        assertThat(formattedNoCorrectAnswerWithAnswerMessages, is(equalTo(ImmutableList.of(
                "It looks like no one was able to answer that one! The correct answer was \"some answer\".\n\nscores\n\nOK, <@U12345>, let's try another one!"
        ))));
        assertThat(formattedNoCorrectAnswerWithoutAnswerMessages, is(equalTo(ImmutableList.of(
                "It looks like no one was able to answer that one!\n\nscores\n\nOK, <@U12345>, let's try another one!"
        ))));

        assertThat(cut.getCorrectAnswerMessages(), is(notNullValue()));
        final List<String> formattedCorrectAnswerWithAnswerMessages = cut.getCorrectAnswerMessages().stream()
                .map(messagesByAnswerType -> messagesByAnswerType.get(AnswerType.WITH_ANSWER))
                .map(message -> String.format(message, "some answer", "scores", userId1))
                .collect(Collectors.toList());
        final List<String> formattedCorrectAnswerWithoutAnswerMessages = cut.getCorrectAnswerMessages().stream()
                .map(messagesByAnswerType -> messagesByAnswerType.get(AnswerType.WITHOUT_ANSWER))
                .map(message -> String.format(message, null, "scores", userId1))
                .collect(Collectors.toList());
        assertThat(formattedCorrectAnswerWithAnswerMessages, is(equalTo(ImmutableList.of(
                "<@U12345> is correct with \"some answer\"!\n\nscores\n\nOK, <@U12345>, you're up!"
        ))));
        assertThat(formattedCorrectAnswerWithoutAnswerMessages, is(equalTo(ImmutableList.of(
                "<@U12345> is correct!\n\nscores\n\nOK, <@U12345>, you're up!"
        ))));
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
