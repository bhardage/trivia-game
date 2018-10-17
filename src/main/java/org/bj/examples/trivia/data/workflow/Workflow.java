package org.bj.examples.trivia.data.workflow;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class Workflow {
    public static final String CHANNEL_ID_KEY = "channelId";
    public static final String CONTROLLING_USER_ID_KEY = "controllingUserId";
    public static final String QUESTION_KEY = "question";
    public static final String ANSWERS_KEY = "answers";
    public static final String STAGE_KEY = "stage";

    @Id
    private ObjectId id;
    private String channelId;
    private String controllingUserId;
    private String question;
    private List<Answer> answers = new ArrayList<>();
    private WorkflowStage stage;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getControllingUserId() {
        return controllingUserId;
    }

    public void setControllingUserId(String controllingUserId) {
        this.controllingUserId = controllingUserId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public WorkflowStage getStage() {
        return stage;
    }

    public void setStage(WorkflowStage stage) {
        this.stage = stage;
    }
}
