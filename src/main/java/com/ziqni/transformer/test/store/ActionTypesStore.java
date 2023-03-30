package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import lombok.NonNull;
import scala.Option;

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
        return null;
    }

    public CompletableFuture<Optional<Result>> create(final String action, Option<String> name, Option<scala.collection.Map<String, String>> metaData, String unitOfMeasureKey) {
        return null;
    }

    public CompletableFuture<ModelApiResponse> update(String action, Option<String> name, Option<scala.collection.Map<String, String>> metaData, Option<String> unitOfMeasureType) {
        return null;
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
        return new ActionTypeEntry("action-key" + identifierCount, "actiontyp-" + identifierCount, "TestActionName");
    }

    public static class ActionTypeEntry {
        public final String key;
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
