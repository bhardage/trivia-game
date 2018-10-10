package org.bj.examples.trivia.dao.workflow;

import org.bj.examples.trivia.dao.BaseDao;
import org.bj.examples.trivia.dao.score.ScoreInfo;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

public class WorkflowDao extends BaseDao {
    public WorkflowDao() {
        super("Workflow");
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
            final IncompleteKey key = keyFactory.newKey();
            workflowEntity = datastore.add(workflowToEntity(key, workflow));
        } else {
            final Key key = keyFactory.newKey(workflow.getId());
            datastore.update((Entity)workflowToEntity(key, workflow));
        }

        return entityToWorkflow(workflowEntity);
    }

    private FullEntity<?> workflowToEntity(final IncompleteKey key, final Workflow workflow) {
        return Entity.newBuilder(key)
                .set(Workflow.CHANNEL_ID_KEY, workflow.getChannelId())
                .set(Workflow.CONTROLLING_USER_ID_KEY, workflow.getControllingUserId())
                .set(Workflow.STAGE_KEY, workflow.getStage().toString())
                .build();
    }

    private Workflow entityToWorkflow(final Entity entity) {
        if (entity == null) {
            return null;
        }

        return new Workflow.Builder()
                .id(entity.getKey().getId())
                .channelId(entity.getString(Workflow.CHANNEL_ID_KEY))
                .controllingUserId(entity.getString(Workflow.CONTROLLING_USER_ID_KEY))
                .stage(WorkflowStage.valueOf(entity.getString(Workflow.STAGE_KEY)))
                .build();
    }
}
