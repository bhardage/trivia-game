package org.bj.examples.trivia.controller;

import org.bj.examples.trivia.dto.SlackSlashCommandRequestDoc;
import org.bj.examples.trivia.dto.SlackSlashCommandResponseDoc;
import org.bj.examples.trivia.service.SlackSlashCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SlackController {
    private final SlackSlashCommandService slackSlashCommandService;

    @Autowired
    public SlackController(final SlackSlashCommandService slackSlashCommandService) {
        this.slackSlashCommandService = slackSlashCommandService;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public SlackSlashCommandResponseDoc slackSlashCommand(final SlackSlashCommandRequestDoc requestDoc) {
        return slackSlashCommandService.processSlashCommand(requestDoc);
    }
}
