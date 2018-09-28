package org.bj.examples.trivia.service;

import java.util.Arrays;
import java.util.List;

import org.bj.examples.trivia.dto.SlackAttachment;
import org.bj.examples.trivia.dto.SlackResponseType;
import org.bj.examples.trivia.dto.SlackSlashCommandRequestDoc;
import org.bj.examples.trivia.dto.SlackSlashCommandResponseDoc;
import org.springframework.stereotype.Service;

@Service
public class SlackSlashCommandServiceImpl implements SlackSlashCommandService {
    @Override
    public SlackSlashCommandResponseDoc processSlashCommand(final SlackSlashCommandRequestDoc requestDoc) {
        final SlackSlashCommandResponseDoc responseDoc = new SlackSlashCommandResponseDoc();

        responseDoc.setResponseType(SlackResponseType.EPHEMERAL);
        responseDoc.setText("Here's what you submitted:");

        final List<SlackAttachment> attachments = Arrays.asList(
                new SlackAttachment("Token: " + requestDoc.getToken()),
                new SlackAttachment("TeamId: " + requestDoc.getTeamId()),
                new SlackAttachment("TeamDomain: " + requestDoc.getTeamDomain()),
                new SlackAttachment("EnterpriseId: " + requestDoc.getEnterpriseId()),
                new SlackAttachment("EnterpriseName: " + requestDoc.getEnterpriseName()),
                new SlackAttachment("ChannelId: " + requestDoc.getChannelId()),
                new SlackAttachment("ChannelName: " + requestDoc.getChannelName()),
                new SlackAttachment("UserId: " + requestDoc.getUserId()),
                new SlackAttachment("Username: " + requestDoc.getUsername()),
                new SlackAttachment("Command: " + requestDoc.getCommand()),
                new SlackAttachment("Text: " + requestDoc.getText()),
                new SlackAttachment("ResponseUrl: " + requestDoc.getResponseUrl()),
                new SlackAttachment("TriggerId: " + requestDoc.getTriggerId())
        );
        responseDoc.setAttachments(attachments);

        return responseDoc;
    }

    public SlackSlashCommandResponseDoc start() {
        return SlackSlashCommandResponseDoc.EMPTY;
    }

    public SlackSlashCommandResponseDoc submitQuestion() {
        return SlackSlashCommandResponseDoc.EMPTY;
    }

    public SlackSlashCommandResponseDoc submitAnswer() {
        return SlackSlashCommandResponseDoc.EMPTY;
    }

    public SlackSlashCommandResponseDoc markAnswerCorrect() {
        return SlackSlashCommandResponseDoc.EMPTY;
    }

    /**
     * This method is used when a person is supposed to be
     * selecting a quote but they don't want to
     */
    public SlackSlashCommandResponseDoc stop() {
        return SlackSlashCommandResponseDoc.EMPTY;
    }

    public SlackSlashCommandResponseDoc getScores() {
        return SlackSlashCommandResponseDoc.EMPTY;
    }

    public SlackSlashCommandResponseDoc resetScores() {
        return SlackSlashCommandResponseDoc.EMPTY;
    }
}
