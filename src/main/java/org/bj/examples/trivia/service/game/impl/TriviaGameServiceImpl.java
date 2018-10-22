package org.bj.examples.trivia.service.game.impl;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bj.examples.trivia.dto.GameState;
import org.bj.examples.trivia.dto.SlackAttachment;
import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.dto.SlackResponseType;
import org.bj.examples.trivia.dto.SlackUser;
import org.bj.examples.trivia.exception.GameNotStartedException;
import org.bj.examples.trivia.exception.ScoreException;
import org.bj.examples.trivia.exception.WorkflowException;
import org.bj.examples.trivia.message.MessageManager;
import org.bj.examples.trivia.message.MessageType;
import org.bj.examples.trivia.service.game.TriviaGameService;
import org.bj.examples.trivia.service.score.ScoreService;
import org.bj.examples.trivia.service.slack.DelayedSlackService;
import org.bj.examples.trivia.service.workflow.WorkflowService;
import org.bj.examples.trivia.util.SlackUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TriviaGameServiceImpl implements TriviaGameService {
    private static final String GAME_NOT_STARTED_FORMAT = "A game has not yet been started. If you'd like to start a game, try `%s start`";

    private static final String BASE_STATUS_FORMAT = "*Topic:* %s\n*Turn:* %s\n*Question:*%s";
    private static final String ANSWERS_FORMAT = "\n\n*Answers:*%s";
    private static final String SINGLE_ANSWER_FORMAT = "%22s   %s   %s";

    private static final String NO_CORRECT_ANSWER_TARGET = "none";
    private static final String SCORES_FORMAT = "```Scores:\n\n%s```";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a").withZone(ZoneId.of("US/Central"));

    private final ScoreService scoreService;
    private final WorkflowService workflowService;
    private final DelayedSlackService delayedSlackService;
    private final MessageManager messageManager;

    @Autowired
    public TriviaGameServiceImpl(
            final ScoreService scoreService,
            final WorkflowService workflowService,
            final DelayedSlackService delayedSlackService,
            final MessageManager messageManager
    ) {
        this.scoreService = scoreService;
        this.workflowService = workflowService;
        this.delayedSlackService = delayedSlackService;
        this.messageManager = messageManager;
    }

    public SlackResponseDoc start(final SlackRequestDoc requestDoc, final String topic) {
        final String channelId = requestDoc.getChannelId();
        final String userId = requestDoc.getUserId();

        try {
            workflowService.onGameStarted(channelId, userId, topic);
        } catch (GameNotStartedException e) {
            return SlackResponseDoc.failure(String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand()));
        } catch (WorkflowException e) {
            return SlackResponseDoc.failure(e.getMessage());
        }

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        responseDoc.setText(messageManager.getMessage(MessageType.GAME_START, userId));

        return responseDoc;
    }

    public SlackResponseDoc stop(final SlackRequestDoc requestDoc) {
        try {
            workflowService.onGameStopped(requestDoc.getChannelId(), requestDoc.getUserId());
        } catch (GameNotStartedException e) {
            return SlackResponseDoc.failure(String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand()));
        } catch (WorkflowException e) {
            return SlackResponseDoc.failure(e.getMessage());
        }

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        responseDoc.setText(messageManager.getMessage(MessageType.GAME_STOP, requestDoc.getCommand(), requestDoc.getUserId()));

        return responseDoc;
    }

    public SlackResponseDoc join(final SlackRequestDoc requestDoc) {
        final SlackUser user = new SlackUser(requestDoc.getUserId(), requestDoc.getUsername());
        final boolean userCreated = scoreService.createUserIfNotExists(requestDoc.getChannelId(), user);

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);

        if (userCreated) {
            responseDoc.setText("Joining game.");

            final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
            delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
            delayedResponseDoc.setText(messageManager.getMessage(MessageType.PLAYER_ADDED, requestDoc.getUserId()));
            delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);
        } else {
            responseDoc.setText("You're already in the game.");
        }

        return responseDoc;
    }

    public SlackResponseDoc pass(final SlackRequestDoc requestDoc, final String target) {
        final String userId = SlackUtils.normalizeId(target);

        try {
            final boolean userExists = scoreService.doesUserExist(requestDoc.getChannelId(), userId);

            if (!userExists) {
                final SlackResponseDoc responseDoc = SlackResponseDoc.failure("User " + target + " does not exist. Please choose a valid user.");
                responseDoc.setAttachments(Arrays.asList(new SlackAttachment("Usage: `" + requestDoc.getCommand() + " pass @jsmith`")));
                return responseDoc;
            }

            workflowService.onTurnChanged(requestDoc.getChannelId(), requestDoc.getUserId(), userId);
        } catch (GameNotStartedException e) {
            return SlackResponseDoc.failure(String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand()));
        } catch (WorkflowException e) {
            return SlackResponseDoc.failure(e.getMessage());
        }

        final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
        delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        delayedResponseDoc.setText(messageManager.getMessage(MessageType.TURN_PASSED, requestDoc.getUserId(), userId));
        delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("Turn passed to <@" + userId + ">.");
        return responseDoc;
    }

    public SlackResponseDoc submitQuestion(final SlackRequestDoc requestDoc, final String question) {
        try {
            workflowService.onQuestionSubmitted(requestDoc.getChannelId(), requestDoc.getUserId(), question);
        } catch (GameNotStartedException e) {
            return SlackResponseDoc.failure(String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand()));
        } catch (WorkflowException e) {
            return SlackResponseDoc.failure(e.getMessage());
        }

        final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
        delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        delayedResponseDoc.setText(messageManager.getMessage(MessageType.QUESTION_SUBMITTED, requestDoc.getUserId(), question));
        delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("Question posted.");

        return responseDoc;
    }

    public SlackResponseDoc submitAnswer(final SlackRequestDoc requestDoc, final String answer) {
        try {
            workflowService.onAnswerSubmitted(
                    requestDoc.getChannelId(),
                    requestDoc.getUserId(),
                    requestDoc.getUsername(),
                    answer,
                    requestDoc.getRequestTime()
            );
        } catch (GameNotStartedException e) {
            return SlackResponseDoc.failure(String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand()));
        } catch (WorkflowException e) {
            return SlackResponseDoc.failure(e.getMessage());
        }

        final SlackUser user = new SlackUser(requestDoc.getUserId(), requestDoc.getUsername());
        scoreService.createUserIfNotExists(requestDoc.getChannelId(), user);

        final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
        delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        delayedResponseDoc.setText(messageManager.getMessage(MessageType.ANSWER_SUBMITTED, requestDoc.getUserId()));
        delayedResponseDoc.setAttachments(Arrays.asList(new SlackAttachment(answer, false)));
        delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("Answer submitted.");

        return responseDoc;
    }

    public SlackResponseDoc markAnswerIncorrect(final SlackRequestDoc requestDoc, final String target) {
        final String userId = SlackUtils.normalizeId(target);

        try {
            workflowService.onIncorrectAnswerSelected(requestDoc.getChannelId(), requestDoc.getUserId(), userId);
        } catch (GameNotStartedException e) {
            return SlackResponseDoc.failure(String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand()));
        } catch (WorkflowException e) {
            return SlackResponseDoc.failure(e.getMessage());
        }

        final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
        delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        delayedResponseDoc.setText(messageManager.getMessage(MessageType.INCORRECT_ANSWER, requestDoc.getUserId()));
        delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("Marked answer incorrect.");
        return responseDoc;
    }

    public SlackResponseDoc markAnswerCorrect(final SlackRequestDoc requestDoc, final String target, final String answer) {
        String text;

        try {
            workflowService.onCorrectAnswerSelected(requestDoc.getChannelId(), requestDoc.getUserId());

            if (target.equalsIgnoreCase(NO_CORRECT_ANSWER_TARGET)) {
                //"Change" back to the original host to reset the workflow state
                workflowService.onTurnChanged(requestDoc.getChannelId(), requestDoc.getUserId(), requestDoc.getUserId());

                text = messageManager.getAnswerMessage(MessageType.NO_CORRECT_ANSWER, answer, generateScoreText(requestDoc), requestDoc.getUserId());
            } else {
                final String userId = SlackUtils.normalizeId(target);

                scoreService.incrementScore(requestDoc.getChannelId(), userId);
                workflowService.onTurnChanged(requestDoc.getChannelId(), requestDoc.getUserId(), userId);

                text = messageManager.getAnswerMessage(MessageType.CORRECT_ANSWER, answer, generateScoreText(requestDoc), userId);
            }
        } catch (GameNotStartedException e) {
            return SlackResponseDoc.failure(String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand()));
        } catch (WorkflowException e) {
            return SlackResponseDoc.failure(e.getMessage());
        } catch (ScoreException e) {
            final SlackResponseDoc responseDoc = SlackResponseDoc.failure("User " + target + " does not exist. Please choose a valid user.");
            responseDoc.setAttachments(Arrays.asList(new SlackAttachment("Usage: `" + requestDoc.getCommand() + " correct @jsmith Blue skies`")));
            return responseDoc;
        }

        final SlackResponseDoc delayedResponseDoc = new SlackResponseDoc();
        delayedResponseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        delayedResponseDoc.setText(text);
        delayedSlackService.sendResponse(requestDoc.getResponseUrl(), delayedResponseDoc);

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("Score updated.");
        return responseDoc;
    }

    public SlackResponseDoc getStatus(final SlackRequestDoc requestDoc) {
        final GameState gameState = workflowService.getCurrentGameState(requestDoc.getChannelId());

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText(generateStatusText(requestDoc, gameState));

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

        final SlackResponseDoc responseDoc = new SlackResponseDoc();
        responseDoc.setResponseType(SlackResponseType.IN_CHANNEL);
        responseDoc.setText("Scores have been reset!");
        responseDoc.setAttachments(Arrays.asList(new SlackAttachment(generateScoreText(requestDoc))));

        return responseDoc;
    }

    private String generateStatusText(final SlackRequestDoc requestDoc, final GameState gameState) {
        if (gameState == null || gameState.getControllingUserId() == null) {
            return String.format(GAME_NOT_STARTED_FORMAT, requestDoc.getCommand());
        }

        final boolean isControllingUser = gameState.getControllingUserId().equals(requestDoc.getUserId());

        final String topic = gameState.getTopic() == null ? "None" : gameState.getTopic();
        final String turn = isControllingUser ? "Yours" : "<@" + gameState.getControllingUserId() + ">";
        final String question = gameState.getQuestion() == null ? " Waiting..." : ("\n\n" + gameState.getQuestion());

        String statusText = String.format(BASE_STATUS_FORMAT, topic, turn, question);

        if (gameState.getQuestion() != null) {
            String answerText;

            if (CollectionUtils.isEmpty(gameState.getAnswers())) {
                answerText = " Waiting...";
            } else {
                int maxUsernameLength = 1 + gameState.getAnswers().stream()
                        .map(GameState.Answer::getUsername)
                        .map(String::length)
                        .max(Comparator.comparing(Integer::valueOf))
                        .orElse(0);

                answerText = "\n\n```" + gameState.getAnswers().stream()
                        .sorted(Comparator.comparing(GameState.Answer::getCreatedDate))
                        .map(answer ->
                            String.format(
                                    SINGLE_ANSWER_FORMAT,
                                    DATE_FORMATTER.format(answer.getCreatedDate().atZone(ZoneId.of("UTC"))),
                                    String.format("@%-" + maxUsernameLength + "s", answer.getUsername()),
                                    answer.getText()
                            )
                        )
                        .collect(Collectors.joining("\n")) + "```";
            }

            statusText += String.format(ANSWERS_FORMAT, answerText);
        }

        return statusText;
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
                    //order by score desc, username
                    .sorted(
                            Map.Entry.<SlackUser, Long>comparingByValue()
                                    .reversed()
                                    .thenComparing(Map.Entry.<SlackUser, Long>comparingByKey(Comparator.comparing(SlackUser::getUsername)))
                    )
                    .map(entry -> String.format("@%-" + maxUsernameLength + "s %3d", entry.getKey().getUsername() + ":", entry.getValue()))
                    .collect(Collectors.joining("\n"));
        }

        return String.format(SCORES_FORMAT, scoreText);
    }
}
