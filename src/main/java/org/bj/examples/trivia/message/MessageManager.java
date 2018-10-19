package org.bj.examples.trivia.message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

@Component
public class MessageManager {
    //Maintain a random number generator for each message type
    //so that message randomness will be preserved _within_ each type
    private final Map<MessageType, Random> randomsByMessageType;

    private final Map<MessageType, List<String>> messageFormatsByType;
    private final Map<MessageType, List<Map<AnswerType, String>>> answerMessageFormatsByType;

    @Autowired
    public MessageManager(final MessageConfig messageConfig) {
        randomsByMessageType = Arrays.stream(MessageType.values())
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                value -> new Random()
                        )
                );
        messageFormatsByType = ImmutableMap.<MessageType, List<String>>builder()
                .put(MessageType.GAME_START, messageConfig.getGameStartMessages())
                .put(MessageType.GAME_STOP, messageConfig.getGameStopMessages())
                .put(MessageType.PLAYER_ADDED, messageConfig.getPlayerAddedMessages())
                .put(MessageType.TURN_PASSED, messageConfig.getTurnPassedMessages())
                .put(MessageType.QUESTION_SUBMITTED, messageConfig.getQuestionSubmittedMessages())
                .put(MessageType.ANSWER_SUBMITTED, messageConfig.getAnswerSubmittedMessages())
                .put(MessageType.INCORRECT_ANSWER, messageConfig.getIncorrectAnswerMessages())
                .build();
        answerMessageFormatsByType = ImmutableMap.of(
                MessageType.NO_CORRECT_ANSWER, messageConfig.getNoCorrectanswerMessages(),
                MessageType.CORRECT_ANSWER, messageConfig.getCorrectAnswerMessages()
        );
    }

    public String getMessage(final MessageType messageType, Object... args) {
        final Random rand = randomsByMessageType.get(messageType);
        final List<String> messageFormats = messageFormatsByType.get(messageType);

        if (messageFormats == null) {
            throw new UnsupportedOperationException();
        }

        if (messageFormats.size() == 1) {
            return String.format(messageFormats.get(0), args);
        }

        return String.format(messageFormats.get(rand.nextInt(messageFormats.size())), args);
    }

    public String getAnswerMessage(final MessageType messageType, final String answer, Object... args) {
        final Random rand = randomsByMessageType.get(messageType);
        final AnswerType answerType = answer == null ? AnswerType.WITHOUT_ANSWER : AnswerType.WITH_ANSWER;
        final List<Map<AnswerType, String>> messagesByAnswerType = answerMessageFormatsByType.get(messageType);

        if (messagesByAnswerType == null) {
            throw new UnsupportedOperationException();
        }

        final Object[] allArgs = new Object[args.length + 1];
        allArgs[0] = answer;
        System.arraycopy(args, 0, allArgs, 1, args.length);

        if (messagesByAnswerType.size() == 1) {
            return String.format(messagesByAnswerType.get(0).get(answerType), allArgs);
        }

        return String.format(messagesByAnswerType.get(rand.nextInt(messagesByAnswerType.size())).get(answerType), allArgs);
    }
}
