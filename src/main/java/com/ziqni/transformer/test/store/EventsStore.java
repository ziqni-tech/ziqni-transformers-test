package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.ziqni.admin.sdk.model.ModelApiResponse;
import com.ziqni.transformers.domain.BasicEventModel;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class EventsStore implements CacheLoader<@NonNull String, EventsStore.EventTransaction> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();
    @Override
    public @Nullable EventsStore.EventTransaction load(@NonNull String key) throws Exception {
        return null;
    }

    public CompletableFuture<ModelApiResponse> pushEvent(BasicEventModel basicEventModel) {
        return pushEvent(List.of(basicEventModel));
    }

    public CompletableFuture<ModelApiResponse> pushEvent(List<BasicEventModel> basicEventModels) {
        return null;
    }

    public CompletableFuture<ModelApiResponse> pushEventTransaction(BasicEventModel basicEventModel) {
        return null;
    }

    public CompletableFuture<List<BasicEventModel>> findByBatchId(String batchId) {
        return null;
    }

    public EventTransaction makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        final var eventTrans = new EventTransaction();
        eventTrans.addBasicEvent(new BasicEventModel(null, null, null, null, null, null, 2.0, null, null, null, null));
        return eventTrans;
    }

    public static class EventTransaction {
        private final List<BasicEventModel> buffer = new ArrayList<>();

        public boolean addBasicEvent(BasicEventModel e) {
            return buffer.add(e);
        }

        public List<BasicEventModel> getEvents() {
            return buffer;
        }
    }
}
