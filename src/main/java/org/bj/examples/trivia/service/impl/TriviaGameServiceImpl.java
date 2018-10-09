package org.bj.examples.trivia.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bj.examples.trivia.dto.SlackAttachment;
import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.dto.SlackResponseType;
import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.ScoreException;
import org.bj.examples.trivia.service.DelayedSlackService;
import org.bj.examples.trivia.service.ScoreService;
import org.bj.examples.trivia.service.TriviaGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TriviaGameServiceImpl implements TriviaGameService {
    private static final String SCORES_FORMAT = "```Scores:\n\n%s```";
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^<@(.+?)(\\|.+)?>$");

    private final ScoreService scoreService;
    private final DelayedSlackService delayedSlackService;

    private SlackUser currentHost = null;
    private boolean questionSubmitted = false;

    @Autowired
    public TriviaGameServiceImpl(
            final ScoreService scoreService,
            final DelayedSlackService delayedSlackService
    ) {
        this.scoreService = scoreService;
        this.delayedSlackService = delayedSlackService;
    }

    public SlackResponseDoc start(final SlackRequestDoc requestDoc) {
        final SlackUser requestUser = new SlackUser(requestDoc.getUserId(), requestDoc.getUsername());

        if (currentHost != null) {
            //Generate a failure response doc
            final SlackResponseDoc responseDoc = new SlackResponseDoc();
            responseDoc.setResponseType(SlackResponseType.EPHEMERAL);

            if (currentHost.equals(requestUser)) {
                responseDoc.setText("You are already hosting!");
            } else {
                responseDoc.setText("<@" + currentHost.getUserId() + "> is currently hosting. If <@" + currentHost.getUserId() + "> is not responding, try `/moviegame my-turn` to request control");
            }

            return responseDoc;
        }

        currentHost = requestUser;

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        responseDoc.setText("OK, <@" + requestUser.getUserId() + ">, please ask a question.");

        return responseDoc;
    }

    /**
     * This method is used when a person is supposed to be
     * selecting a quote but they don't want to
     */
    public SlackResponseDoc stop(final SlackRequestDoc requestDoc) {
        return SlackResponseDoc.EMPTY;
    }

    public SlackResponseDoc submitQuestion(final SlackRequestDoc requestDoc, final String question) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);

        if (currentHost == null) {
            responseDoc.setText("A game has not yet been started. If you'd like to start a game, try `/moviegame start`");

            return responseDoc;
        } else if (!currentHost.getUserId().equals(requestDoc.getUserId())) {
            responseDoc.setText("It's <@" + currentHost.getUserId() + ">'s turn to ask a question.");

            return responseDoc;
        }

        questionSubmitted = true;

        final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
        delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        delayedResponseDoc.setText("<@" + requestDoc.getUserId() + "> asked the following question:\n\n" + question);
        delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);

        responseDoc.setText("Question posted.");

        return responseDoc;
    }

    public SlackResponseDoc submitAnswer(final SlackRequestDoc requestDoc, final String answer) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        if (currentHost == null) {
            responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
            responseDoc.setText("A game has not yet been started. If you'd like to start a game, try `/moviegame start`");

            return responseDoc;
        } else if (!questionSubmitted) {
            responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
            responseDoc.setText("A question has not yet been submitted. Please wait for <@" + currentHost.getUserId() + "> to ask a question.");

            return responseDoc;
        }

        final SlackUser user = new SlackUser(requestDoc.getUserId(), requestDoc.getUsername());

        scoreService.createUserIfNotExists(requestDoc.getChannelId(), user);

        responseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        responseDoc.setText(null);

        return responseDoc;
    }

    public SlackResponseDoc markAnswerCorrect(final SlackRequestDoc requestDoc, final String target, final String answer) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);

        if (currentHost == null) {
            responseDoc.setText("A game has not yet been started. If you'd like to start a game, try `/moviegame start`");

            return responseDoc;
        } else if (!currentHost.getUserId().equals(requestDoc.getUserId())) {
            responseDoc.setText("It's <@" + currentHost.getUserId() + ">'s question. Only he/she can mark an answer correct.");

            return responseDoc;
        } else if (!questionSubmitted) {
            responseDoc.setText("A question has not yet been submitted. Please ask a question before marking an answer correct.");

            return responseDoc;
        }

        String userId = target;
        final Matcher userIdMatcher = USER_ID_PATTERN.matcher(target);

        if (userIdMatcher.find()) {
            userId = userIdMatcher.group(1);
        }

        try {
            scoreService.incrementScore(requestDoc.getChannelId(), userId);
        } catch (ScoreException e) {
            responseDoc.setText("User " + target + " does not exist. Please choose a valid user.");
            responseDoc.setAttachments(Arrays.asList(new SlackAttachment("Usage: `/moviegame correct @jsmith Blue skies`")));

            return responseDoc;
        }

        currentHost = new SlackUser(userId, null);
        questionSubmitted = false;

        final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
        delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);

        String text = "<@" + userId + "> is correct";

        if (answer != null) {
            text += " with " + answer;
        }

        text += "!\n\n";
        text += generateScoreText(requestDoc);
        text += "\n\nOK, <@" + userId + ">, you're up!";

        delayedResponseDoc.setText(text);
        delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);

        responseDoc.setText("Score updated.");
        return responseDoc;
    }

    public SlackResponseDoc getScores(final SlackRequestDoc requestDoc) {
        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText(generateScoreText(requestDoc));

        return responseDoc;
    }

    public SlackResponseDoc resetScores(final SlackRequestDoc requestDoc) {
        scoreService.resetScores(requestDoc.getChannelId());

        //TODO If this remains across all channels, let them all know scores have been reset

        final SlackResponseDoc responseDoc = new SlackResponseDoc();

        responseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        responseDoc.setText("Scores have been reset!");
        responseDoc.setAttachments(Arrays.asList(new SlackAttachment(generateScoreText(requestDoc))));

        return responseDoc;
    }

    private String generateScoreText(final SlackRequestDoc requestDoc) {
        final Map<SlackUser, Long> scoresByUser = scoreService.getAllScoresByUser(requestDoc.getChannelId());

        final String scoreText;

        if (scoresByUser.isEmpty()) {
            scoreText = "No scores yet...";
        } else {
            int maxUsernameLength = 1 + scoresByUser.keySet().stream()
                    .map(SlackUser::getUsername)
                    .map(String::length)
                    .max(Comparator.comparing(Integer::valueOf))
                    .orElse(0);
            scoreText = scoresByUser.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(SlackUser::getUsername)))
                    .map(entry -> String.format("@%-" + maxUsernameLength + "s %3d", entry.getKey().getUsername() + ":", entry.getValue()))
                    .collect(Collectors.joining("\n"));
        }

        return String.format(SCORES_FORMAT, scoreText);
    }
}
