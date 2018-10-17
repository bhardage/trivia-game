package org.bj.examples.trivia.data;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;

public abstract class BaseDao {
    protected final Datastore datastore;
    protected final KeyFactory keyFactory;
    protected final String kind;

    public BaseDao(final String kind) {
        this.kind = kind;
        datastore = DatastoreOptions.getDefaultInstance().getService();
        keyFactory = datastore.newKeyFactory().setKind(kind);
    }

    protected <T> Stream<T> asStream(Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
