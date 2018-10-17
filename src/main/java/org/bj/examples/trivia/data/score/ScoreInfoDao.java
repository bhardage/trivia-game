package org.bj.examples.trivia.data.score;

import java.util.List;
import java.util.stream.Collectors;

import org.bj.examples.trivia.data.BaseDao;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

@Service
public class ScoreInfoDao extends BaseDao {
    public ScoreInfoDao() {
        super("ScoreInfo");
    }

    public List<ScoreInfo> findAllByChannelId(final String channelId) {
        final Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(PropertyFilter.eq(ScoreInfo.CHANNEL_ID_KEY, channelId))
                .setOrderBy(OrderBy.desc(ScoreInfo.SCORE_KEY))
                .build();

        return asStream(datastore.run(query))
                .map(this::entityToScoreInfo)
                .collect(Collectors.toList());
    }

    public ScoreInfo findByChannelIdAndUserId(final String channelId, final String userId) {
        final Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(CompositeFilter.and(
                        PropertyFilter.eq(ScoreInfo.CHANNEL_ID_KEY, channelId),
                        PropertyFilter.eq(ScoreInfo.USER_ID_KEY, userId)
                ))
                .build();

        final QueryResults<Entity> results = datastore.run(query);

        if (results.hasNext()) {
            return entityToScoreInfo(results.next());
        } else {
            return null;
        }
    }

    public ScoreInfo save(final ScoreInfo scoreInfo) {
        if (scoreInfo == null) {
            return null;
        }

        Entity scoreInfoEntity = null;

        if (scoreInfo.getId() == null) {
            final IncompleteKey key = keyFactory.newKey(new ObjectId().toHexString());
            scoreInfoEntity = datastore.add(scoreInfoToEntity(key, scoreInfo));
        } else {
            final Key key = keyFactory.newKey(scoreInfo.getId().toHexString());
            datastore.update(scoreInfoToEntity(key, scoreInfo));
        }

        return entityToScoreInfo(scoreInfoEntity);
    }

    public void deleteAllByChannelId(final String channelId) {
        final Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(PropertyFilter.eq(ScoreInfo.CHANNEL_ID_KEY, channelId))
                .build();

        final List<Key> keys = asStream(datastore.run(query))
                .map(entity -> entity.getKey())
                .collect(Collectors.toList());

        datastore.delete(keys.toArray(new Key[0]));
    }

    private FullEntity<IncompleteKey> scoreInfoToEntity(final IncompleteKey key, final ScoreInfo scoreInfo) {
        return Entity.newBuilder(key)
                .set(ScoreInfo.CHANNEL_ID_KEY, scoreInfo.getChannelId())
                .set(ScoreInfo.USER_ID_KEY, scoreInfo.getUserId())
                .set(ScoreInfo.USERNAME_KEY, scoreInfo.getUsername())
                .set(ScoreInfo.SCORE_KEY, scoreInfo.getScore())
                .build();
    }

    private Entity scoreInfoToEntity(final Key key, final ScoreInfo scoreInfo) {
        return Entity.newBuilder(key)
                .set(ScoreInfo.CHANNEL_ID_KEY, scoreInfo.getChannelId())
                .set(ScoreInfo.USER_ID_KEY, scoreInfo.getUserId())
                .set(ScoreInfo.USERNAME_KEY, scoreInfo.getUsername())
                .set(ScoreInfo.SCORE_KEY, scoreInfo.getScore())
                .build();
    }

    private ScoreInfo entityToScoreInfo(final Entity entity) {
        if (entity == null) {
            return null;
        }

        final ScoreInfo scoreInfo = new ScoreInfo();
        scoreInfo.setId(new ObjectId(entity.getKey().getName()));
        scoreInfo.setChannelId(entity.getString(ScoreInfo.CHANNEL_ID_KEY));
        scoreInfo.setUserId(entity.getString(ScoreInfo.USER_ID_KEY));
        scoreInfo.setUsername(entity.getString(ScoreInfo.USERNAME_KEY));
        scoreInfo.setScore(entity.getLong(ScoreInfo.SCORE_KEY));

        return scoreInfo;
    }
}
