package org.bj.examples.trivia.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlackUtils {
    private static final Pattern SLACK_ID_PATTERN = Pattern.compile("^<@(.+?)(\\|.+)?>$");

    public static String normalizeId(final String slackId) {
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
