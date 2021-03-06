package org.bj.examples.trivia.service.slack;

import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;

public interface SlackSlashCommandService {
    SlackResponseDoc processSlashCommand(final SlackRequestDoc requestDoc);
}
