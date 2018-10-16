package org.bj.examples.trivia.dao.workflow;

import java.util.List;
import java.util.stream.Collectors;

import org.bj.examples.trivia.dao.BaseDao;
import org.bj.examples.trivia.dao.score.ScoreInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.ListValue;
import com.google.cloud.datastore.NullValue;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

@Service
public class WorkflowDao extends BaseDao {
    private final AnswerDao answerDao;

    @Autowired
    public WorkflowDao(final AnswerDao answerDao) {
        super("Workflow");

        this.answerDao = answerDao;
    }

    public Workflow findByChannelId(final String channelId) {
        final Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(PropertyFilter.eq(ScoreInfo.CHANNEL_ID_KEY, channelId))
                .build();

        final QueryResults<Entity> results = datastore.run(query);

        if (results.hasNext()) {
            return entityToWorkflow(results.next());
        } else {
            return null;
        }
    }

    public Workflow save(final Workflow workflow) {
        if (workflow == null) {
            return null;
        }

        Entity workflowEntity = null;

        if (workflow.getId() == null) {
            final IncompleteKey key = keyFactory.newKey(new ObjectId().toHexString());
            workflowEntity = datastore.add(workflowToEntity(key, workflow));
        } else {
            final Key key = keyFactory.newKey(workflow.getId().toHexString());
            datastore.update(workflowToEntity(key, workflow));
        }

        return entityToWorkflow(workflowEntity);
    }

    public void delete(final String keyName) {
        datastore.delete(keyFactory.newKey(keyName));
    }

    private FullEntity<IncompleteKey> workflowToEntity(final IncompleteKey key, final Workflow workflow) {
        final List<EntityValue> answerValues = workflow.getAnswers().stream()
                .map(answerDao::answerToEntity)
                .map(EntityValue::of)
                .collect(Collectors.toList());

        return Entity.newBuilder(key)
                .set(Workflow.CHANNEL_ID_KEY, workflow.getChannelId())
                .set(Workflow.CONTROLLING_USER_ID_KEY, workflow.getControllingUserId())
                .set(Workflow.QUESTION_KEY, workflow.getQuestion() == null ? NullValue.of() : StringValue.of(workflow.getQuestion()))
                .set(Workflow.ANSWERS_KEY, ListValue.of(answerValues))
                .set(Workflow.STAGE_KEY, workflow.getStage().toString())
                .build();
    }

    private Entity workflowToEntity(final Key key, final Workflow workflow) {
        final List<EntityValue> answerValues = workflow.getAnswers().stream()
                .map(answerDao::answerToEntity)
                .map(EntityValue::of)
                .collect(Collectors.toList());

        return Entity.newBuilder(key)
                .set(Workflow.CHANNEL_ID_KEY, workflow.getChannelId())
                .set(Workflow.CONTROLLING_USER_ID_KEY, workflow.getControllingUserId())
                .set(Workflow.QUESTION_KEY, workflow.getQuestion() == null ? NullValue.of() : StringValue.of(workflow.getQuestion()))
                .set(Workflow.ANSWERS_KEY, ListValue.of(answerValues))
                .set(Workflow.STAGE_KEY, workflow.getStage().toString())
                .build();
    }

    private Workflow entityToWorkflow(final Entity entity) {
        if (entity == null) {
            return null;
        }

        final Workflow workflow = new Workflow();
        workflow.setId(new ObjectId(entity.getKey().getName()));
        workflow.setChannelId(entity.getString(Workflow.CHANNEL_ID_KEY));
        workflow.setControllingUserId(entity.getString(Workflow.CONTROLLING_USER_ID_KEY));
        workflow.setQuestion(entity.getString(Workflow.QUESTION_KEY));

        final List<Answer> answers = entity.getList(Workflow.ANSWERS_KEY).stream()
                .map(value -> (FullEntity<?>)value.get())
                .map(answerDao::entityToAnswer)
                .collect(Collectors.toList());
        workflow.setAnswers(answers);

        workflow.setStage(WorkflowStage.valueOf(entity.getString(Workflow.STAGE_KEY)));

        return workflow;
    }
}
