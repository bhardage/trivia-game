package org.bj.examples.trivia.service;

import org.bj.examples.trivia.dto.SlackSlashCommandRequestDoc;
import org.bj.examples.trivia.dto.SlackSlashCommandResponseDoc;

public interface SlackSlashCommandService {
    SlackSlashCommandResponseDoc processSlashCommand(final SlackSlashCommandRequestDoc requestDoc);
}
