package org.bj.examples.trivia.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("messages")
public class MessageConfig {
    private List<String> gameStartMessages = new ArrayList<>();
    private List<String> gameStopMessages = new ArrayList<>();
    private List<String> playerAddedMessages = new ArrayList<>();
    private List<String> turnPassedMessages = new ArrayList<>();
    private List<String> questionSubmittedMessages = new ArrayList<>();
    private List<String> answerSubmittedMessages = new ArrayList<>();
    private List<String> incorrectAnswerMessages = new ArrayList<>();
    private List<Map<AnswerType, String>> noCorrectanswerMessages = new ArrayList<>();
    private List<Map<AnswerType, String>> correctAnswerMessages = new ArrayList<>();

    public List<String> getGameStartMessages() {
        return gameStartMessages;
    }

    public void setGameStartMessages(List<String> gameStartMessages) {
        this.gameStartMessages = gameStartMessages;
    }

    public List<String> getGameStopMessages() {
        return gameStopMessages;
    }

    public void setGameStopMessages(List<String> gameStopMessages) {
        this.gameStopMessages = gameStopMessages;
    }

    public List<String> getPlayerAddedMessages() {
        return playerAddedMessages;
    }

    public void setPlayerAddedMessages(List<String> playerAddedMessages) {
        this.playerAddedMessages = playerAddedMessages;
    }

    public List<String> getTurnPassedMessages() {
        return turnPassedMessages;
    }

    public void setTurnPassedMessages(List<String> turnPassedMessages) {
        this.turnPassedMessages = turnPassedMessages;
    }

    public List<String> getQuestionSubmittedMessages() {
        return questionSubmittedMessages;
    }

    public void setQuestionSubmittedMessages(List<String> questionSubmittedMessages) {
        this.questionSubmittedMessages = questionSubmittedMessages;
    }

    public List<String> getAnswerSubmittedMessages() {
        return answerSubmittedMessages;
    }

    public void setAnswerSubmittedMessages(List<String> answerSubmittedMessages) {
        this.answerSubmittedMessages = answerSubmittedMessages;
    }

    public List<String> getIncorrectAnswerMessages() {
        return incorrectAnswerMessages;
    }

    public void setIncorrectAnswerMessages(List<String> incorrectAnswerMessages) {
        this.incorrectAnswerMessages = incorrectAnswerMessages;
    }

    public List<Map<AnswerType, String>> getNoCorrectanswerMessages() {
        return noCorrectanswerMessages;
    }

    public void setNoCorrectanswerMessages(List<Map<AnswerType, String>> noCorrectanswerMessages) {
        this.noCorrectanswerMessages = noCorrectanswerMessages;
    }

    public List<Map<AnswerType, String>> getCorrectAnswerMessages() {
        return correctAnswerMessages;
    }

    public void setCorrectAnswerMessages(List<Map<AnswerType, String>> correctAnswerMessages) {
        this.correctAnswerMessages = correctAnswerMessages;
    }
}
