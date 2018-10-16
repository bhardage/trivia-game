package org.bj.examples.trivia.dao.workflow;

import java.sql.Date;
import java.time.ZoneId;

import org.bj.examples.trivia.dao.BaseDao;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;

//TODO Move these methods into the WorkflowDao
@Service
public class AnswerDao extends BaseDao {
    public AnswerDao() {
        super("Answer");
    }

    public FullEntity<?> answerToEntity(final Answer answer) {
        final IncompleteKey key = keyFactory.newKey();

        return Entity.newBuilder(key)
                .set(Answer.USER_ID_KEY, answer.getUserId())
                .set(Answer.USERNAME_KEY, answer.getUsername())
                .set(Answer.TEXT_KEY, answer.getText())
                .set(Answer.CREATED_DATE_KEY, Timestamp.of(Date.from(answer.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant())))
                .build();
    }

    public Answer entityToAnswer(final FullEntity<?> entity) {
        if (entity == null) {
            return null;
        }

        final Answer answer = new Answer();
        answer.setUserId(entity.getString(Answer.USER_ID_KEY));
        answer.setUsername(entity.getString(Answer.USERNAME_KEY));
        answer.setText(entity.getString(Answer.TEXT_KEY));
        answer.setCreatedDate(entity.getTimestamp(Answer.CREATED_DATE_KEY).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        return answer;
    }
}
