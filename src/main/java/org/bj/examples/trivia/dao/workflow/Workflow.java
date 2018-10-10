package org.bj.examples.trivia.dao.workflow;

public class Workflow {
    public static final String CHANNEL_ID_KEY = "channelId";
    public static final String CONTROLLING_USER_ID_KEY = "controllingUserId";
    public static final String STAGE_KEY = "stage";

    private Long id;
    private String channelId;
    private String controllingUserId;
    private WorkflowStage stage;

    private Workflow(Builder builder) {
        this.id = builder.id;
        this.channelId = builder.channelId;
        this.controllingUserId = builder.controllingUserId;
        this.stage = builder.stage;
    }

    public static class Builder {
        private Long id;
        private String channelId;
        private String controllingUserId;
        private WorkflowStage stage;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder controllingUserId(String controllingUserId) {
            this.controllingUserId = controllingUserId;
            return this;
        }

        public Builder stage(WorkflowStage stage) {
            this.stage = stage;
            return this;
        }

        public Workflow build() {
            return new Workflow(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public WorkflowStage getStage() {
        return stage;
    }

    public void setStage(WorkflowStage stage) {
        this.stage = stage;
    }
}
