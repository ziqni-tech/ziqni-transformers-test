package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.ApiException;
import com.ziqni.admin.sdk.model.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import lombok.NonNull;
import scala.Option;
import scala.collection.JavaConverters;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionTypesStore implements AsyncCacheLoader<@NonNull String, ActionTypesStore.ActionTypeEntry>, RemovalListener<@NonNull String, ActionTypesStore.ActionTypeEntry> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();

    public final AsyncLoadingCache<String, ActionTypeEntry> cache = Caffeine
            .newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .evictionListener(this).buildAsync(this);

    public CompletableFuture<Boolean> actionTypeExists(String action) {
        return Objects.requireNonNull(this.cache.getIfPresent(action)).thenApply(Objects::nonNull);
    }

    public CompletableFuture<Optional<Result>> create(final String action, Option<String> name, Option<scala.collection.Map<String, String>> metaData, String unitOfMeasureKey) {
        final var out = new CompletableFuture<Optional<Result>>();
        var isInCache = this.cache
                .get(action)
                .thenApply(Objects::nonNull)
                .join();
        if(isInCache)
            out.completeExceptionally(new ApiException("action_type_with_action_[" + action + "]_already_exists")); // or whatever we throw
        else {
            final var actionTypeEntry = makeMock();
            if (!name.isEmpty()){
                actionTypeEntry.setName(name.get());
            }
            this.cache.put(actionTypeEntry.getId(), CompletableFuture.completedFuture(actionTypeEntry));
            out.thenApply(y -> y.orElse(new Result()
                    .id(actionTypeEntry.getId())
                    .result("CREATED")
                    .externalReference(actionTypeEntry.getKey())));
        }

        return out;
    }

    public CompletableFuture<ModelApiResponse> update(String action, Option<String> name, Option<scala.collection.Map<String, String>> metaData, Option<String> unitOfMeasureType) {
        final var out = new CompletableFuture<ModelApiResponse>();
        var isNotInCache = this.cache
                .get(action)
                .thenApply(Objects::isNull)
                .join();
        if(isNotInCache)
            out.completeExceptionally(new ApiException("action_type_with_action_[" + action + "]_does_not_exist")); // or whatever we throw
        else {
            final var actionTypeEntry = makeMock();
            if (!name.isEmpty()){
                actionTypeEntry.setName(name.get());
            }
            this.cache.put(actionTypeEntry.getId(), CompletableFuture.completedFuture(actionTypeEntry));
            out.thenApply(y -> new Result()
                    .id(actionTypeEntry.getId())
                    .result("UPDATED")
                    .externalReference(actionTypeEntry.getKey()));
        }

        return out;
    }

    @Override
    public CompletableFuture<? extends ActionTypesStore.ActionTypeEntry> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return CompletableFuture.completedFuture(makeMock());
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable ActionTypesStore.ActionTypeEntry value, RemovalCause cause) {

    }

    public ActionTypeEntry makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new ActionTypeEntry("action-key" + identifierCount, "actiontyp-" + identifierCount, "TestActionName-" + identifierCount);
    }

    public static class ActionTypeEntry {
        public String key;
        public String id;
        public String name;

        public ActionTypeEntry(String key) {
            this.key = key;
        }

        public ActionTypeEntry(String key, String id, String name) {
            this.key = key;
            this.id = id;
            this.name = name;
        }

        public ActionTypeEntry(ActionType actionType) {
            this.key = actionType.getKey();
            this.id = actionType.getId();
            this.name = actionType.getName();
        }

        public ActionTypeEntry setId(String id) {
            this.id = id;
            return this;
        }

        public ActionTypeEntry setName(String name) {
            this.name = name;
            return this;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ActionTypeEntry)) return false;
            ActionTypeEntry that = (ActionTypeEntry) o;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}
