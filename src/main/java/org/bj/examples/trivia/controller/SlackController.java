package org.bj.examples.trivia.controller;

import org.bj.examples.trivia.dto.SlackRequestDoc;
import org.bj.examples.trivia.dto.SlackResponseDoc;
import org.bj.examples.trivia.service.SlackSlashCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/slack")
public class SlackController {
    private final SlackSlashCommandService slackSlashCommandService;

    @Autowired
    public SlackController(final SlackSlashCommandService slackSlashCommandService) {
        this.slackSlashCommandService = slackSlashCommandService;
    }

    @RequestMapping(value = "/slash", method = RequestMethod.POST)
    public SlackResponseDoc slackSlashCommand(final SlackRequestDoc requestDoc) {
        return slackSlashCommandService.processSlashCommand(requestDoc);
    }
}
