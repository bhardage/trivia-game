package org.bj.examples.trivia.dao.workflow;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkflowRepo extends MongoRepository<Workflow, ObjectId> {
    Workflow findByChannelId(final String channelId);
}
