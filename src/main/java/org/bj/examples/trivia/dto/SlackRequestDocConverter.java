package org.bj.examples.trivia.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.util.StdConverter;

public class SlackRequestDocConverter extends StdConverter<SlackRequestDoc, SlackRequestDoc> {
    private static final Pattern SLACK_ID_PATTERN = Pattern.compile("^<@(.+?)(\\|.+)?>$");

    @Override
    public SlackRequestDoc convert(SlackRequestDoc requestDoc) {
        //Normalize all Slack id's
        requestDoc.setTeamId(normalizeSlackId(requestDoc.getTeamId()));
        requestDoc.setEnterpriseId(normalizeSlackId(requestDoc.getEnterpriseId()));
        requestDoc.setChannelId(normalizeSlackId(requestDoc.getChannelId()));
        requestDoc.setUserId(normalizeSlackId(requestDoc.getUserId()));

        return requestDoc;
    }

    private String normalizeSlackId(final String slackId) {
        if (slackId == null) {
            return null;
        }

        String extractedId = slackId;
        final Matcher userIdMatcher = SLACK_ID_PATTERN.matcher(extractedId);

        if (userIdMatcher.find()) {
            extractedId = userIdMatcher.group(1);
        }

        return extractedId;
    }
}
