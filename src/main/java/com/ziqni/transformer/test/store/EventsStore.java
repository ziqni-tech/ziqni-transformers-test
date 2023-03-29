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

public class EventsStore implements CacheLoader<@NonNull String, EventsStore.EventTransaction> {

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
