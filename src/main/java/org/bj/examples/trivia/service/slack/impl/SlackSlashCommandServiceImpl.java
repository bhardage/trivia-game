package org.bj.examples.trivia.service.slack.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.bj.examples.trivia.dto.SlackAttachment;
import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.dto.SlackResponseType;
import org.bj.examples.trivia.service.game.TriviaGameService;
import org.bj.examples.trivia.service.slack.SlackSlashCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SlackSlashCommandServiceImpl implements SlackSlashCommandService {
    private final TriviaGameService triviaGameService;

    @Autowired
    public SlackSlashCommandServiceImpl(final TriviaGameService triviaGameService) {
        this.triviaGameService = triviaGameService;
    }

    @Override
    public SlackResponseDoc processSlashCommand(final SlackRequestDoc requestDoc) {
        //First thing, capture the timestamp
        requestDoc.setRequestTime(LocalDateTime.now(ZoneId.of("UTC")));

        String commandText = requestDoc.getText() == null ? "" : requestDoc.getText().trim();
        final String[] commandParts = commandText.split("\\s+");

        String operator = null;

        if (commandParts.length >= 1) {
            operator = commandParts[0];
            commandText = commandText.substring(operator.length(), commandText.length()).trim();
        }

        switch (operator) {
            case "start":
                return triviaGameService.start(requestDoc, StringUtils.isEmpty(commandText) ? null : commandText);
            case "stop":
                return triviaGameService.stop(requestDoc);
            case "join":
                return triviaGameService.join(requestDoc);
            case "pass":
                if (commandParts.length < 2) {
                    return getPassFormat(requestDoc.getCommand());
                }

                return triviaGameService.pass(requestDoc, commandText);
            case "question":
                if (commandParts.length < 2) {
                    return getSubmitQuestionFormat(requestDoc.getCommand());
                }

                return triviaGameService.submitQuestion(requestDoc, commandText);
            case "answer":
                if (commandParts.length < 2) {
                    return getSubmitAnswerFormat(requestDoc.getCommand());
                }

                return triviaGameService.submitAnswer(requestDoc, commandText);
            case "incorrect":
                if (commandParts.length < 2) {
                    return getMarkAnswerIncorrectFormat(requestDoc.getCommand());
                }

                return triviaGameService.markAnswerIncorrect(requestDoc, commandText);
            case "correct":
                if (commandParts.length < 2) {
                    return getMarkAnswerCorrectFormat(requestDoc.getCommand());
                }

                return triviaGameService.markAnswerCorrect(
                        requestDoc,
                        commandParts[1],
                        commandParts.length > 2 ? commandText.substring(commandParts[1].length(), commandText.length()).trim() : null
                );
            case "status":
                return triviaGameService.getStatus(requestDoc);
            case "scores":
                return triviaGameService.getScores(requestDoc);
            case "reset":
                return triviaGameService.resetScores(requestDoc);
        }

        return getUsageFormat(requestDoc.getCommand());
    }

    private SlackResponseDoc getPassFormat(final String command) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To pass your turn, use `" + command + " pass <USERNAME>`.\n\nFor example, `" + command + " pass @jsmith`");

        return responseDoc;
    }

    private SlackResponseDoc getSubmitQuestionFormat(final String command) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To submit a question, use `" + command + " question <QUESTION_TEXT>`.\n\nFor example, `" + command + " question In what year did WWII officially begin?`");

        return responseDoc;
    }

    private SlackResponseDoc getSubmitAnswerFormat(final String command) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To submit an answer, use `" + command + " answer <ANSWER_TEXT>`.\n\nFor example, `" + command + " answer Blue skies`");

        return responseDoc;
    }

    private SlackResponseDoc getMarkAnswerIncorrectFormat(final String command) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To identify an answer as incorrect, use `" + command + " incorrect <USERNAME>`.\n"
                //+ "Optional: To include the incorrect answer to which you're referring, use `" + command + " incorrect <USERNAME> <INCORRECT_ANSWER>`.\n\n"
                + "\nFor example, `" + command + " incorrect @jsmith`");

        return responseDoc;
    }

    private SlackResponseDoc getMarkAnswerCorrectFormat(final String command) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To mark an answer correct, use `" + command + " correct <USERNAME>`.\n"
                + "Optional: To include the correct answer, use `" + command + " correct <USERNAME> <CORRECT_ANSWER>`.\n\n"
                + "For example, `" + command + " correct @jsmith Chris Farley`");

        return responseDoc;
    }

    private SlackResponseDoc getUsageFormat(final String command) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("`" + command + "` usage:");

        final List<SlackAttachment> attachments = Arrays.asList(
                new SlackAttachment("To start a new game as the host, use `" + command + " start`"),
                new SlackAttachment("To join a game, use `" + command + " join`"),
                new SlackAttachment("To ask a question, use `" + command + " question <QUESTION>`. This requires you to be the host."),
                new SlackAttachment("To answer a question, use `" + command + " answer <ANSWER>`. (Note that answering a question will automatically join the game.)"),
                new SlackAttachment(
                        "To identify a correct answer, use `" + command + " correct <USERNAME> <ANSWER>`." +
                                " If no correct answers were given, use `" + command + " correct none <CORRECT_ANSWER>`. This requires you to be the host."
                ),
                new SlackAttachment("To pass your turn to someone else, use `" + command + " pass <USERNAME>`"),
                new SlackAttachment("To view whose turn it is, the current question, and all answers provided so far, use `" + command + " status`"),
                new SlackAttachment("To view the current scores, use `" + command + " scores`."),
                new SlackAttachment("To reset all scores, use `" + command + " reset`."),
                new SlackAttachment("To stop the current game, use `" + command + " stop`. This requires you to be the host.")
        );
        responseDoc.setAttachments(attachments);

        return responseDoc;
    }
}
