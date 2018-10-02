package org.bj.examples.trivia.service.impl;

import java.util.Arrays;
import java.util.List;

import org.bj.examples.trivia.dto.SlackAttachment;
import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.dto.SlackResponseType;
import org.bj.examples.trivia.service.SlackSlashCommandService;
import org.bj.examples.trivia.service.TriviaGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlackSlashCommandServiceImpl implements SlackSlashCommandService {
    private final TriviaGameService triviaGameService;

    @Autowired
    public SlackSlashCommandServiceImpl(final TriviaGameService triviaGameService) {
        this.triviaGameService = triviaGameService;
    }

    @Override
    public SlackResponseDoc processSlashCommand(final SlackRequestDoc requestDoc) {
        String commandText = requestDoc.getText() == null ? "" : requestDoc.getText().trim();
        final String[] commandParts = commandText.split("\\s+");

        String operator = null;

        if (commandParts.length >= 1) {
            operator = commandParts[0];
            commandText = commandText.substring(operator.length(), commandText.length()).trim();
        }

        switch (operator) {
            case "start":
                return triviaGameService.start(requestDoc);
            case "stop":
                return triviaGameService.stop(requestDoc);
            case "question":
                if (commandParts.length < 2) {
                    return getSubmitQuestionFormat();
                }

                return triviaGameService.submitQuestion(requestDoc, commandText);
            case "answer":
                if (commandParts.length < 2) {
                    return getSubmitAnswerFormat();
                }

                return triviaGameService.submitAnswer(requestDoc, commandText);
            case "correct":
                if (commandParts.length < 2) {
                    return getMarkAnswerCorrectFormat();
                }

                return triviaGameService.markAnswerCorrect(
                        requestDoc,
                        commandParts[1],
                        commandParts.length > 2 ? commandText.substring(commandParts[1].length(), commandText.length()).trim() : null
                );
            case "scores":
                return triviaGameService.getScores(requestDoc);
            case "reset":
                return triviaGameService.resetScores(requestDoc);
        }

        return getUsageFormat();
    }

    private SlackResponseDoc getSubmitQuestionFormat() {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To submit a question, use `/moviegame question <QUESTION_TEXT>`.\n\nFor example, `/moviegame question In what year did WWII officially begin?`");

        return responseDoc;
    }

    private SlackResponseDoc getSubmitAnswerFormat() {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To submit an answer, use `/moviegame answer <ANSWER_TEXT>`.\n\nFor example, `/moviegame answer Blue skies`");

        return responseDoc;
    }

    private SlackResponseDoc getMarkAnswerCorrectFormat() {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("To mark an answer correct, use `/moviegame correct <USERNAME>`.\n"
                + "Optional: To include the correct answer, use `/moviegame correct <USERNAME> <CORRECT_ANSWER>`.\n\n"
                + "For example, `/moviegame correct @jsmith Chris Farley`");

        return responseDoc;
    }

    private SlackResponseDoc getUsageFormat() {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("`/moviegame` usage:");

        final List<SlackAttachment> attachments = Arrays.asList(
                new SlackAttachment("To start a new game as the host, use `/moviegame start`"),
                new SlackAttachment("To ask a question, use `/moviegame question <QUESTION>`. This requires you to be the host."),
                new SlackAttachment("To answer a question, use `/moviegame answer <ANSWER>`."),
                new SlackAttachment("To identify a correct answer, use `/moviegame correct <USERNAME> <ANSWER>`. This requires you to be the host."),
                new SlackAttachment("To view the current socres, use `/moviegame scores`."),
                new SlackAttachment("To reset all scores, use `/moviegame reset`."),
                new SlackAttachment("To stop the current game and reset scores, use `/moviegame stop`")
        );
        responseDoc.setAttachments(attachments);

        return responseDoc;
    }
}
