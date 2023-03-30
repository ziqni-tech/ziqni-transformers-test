package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.EntityType;
import com.ziqni.admin.sdk.model.Product;
import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.admin.sdk.model.RewardTypeReduced;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicReward;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RewardStore implements AsyncCacheLoader<@NonNull String, @NonNull Reward>, RemovalListener<@NonNull String, @NonNull Reward> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();
    private static final Logger logger = LoggerFactory.getLogger(RewardStore.class);

    public final AsyncLoadingCache<@NonNull String, @NonNull Reward> cache = Caffeine
            .newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public CompletableFuture<Optional<BasicReward>> getBasicReward(String id){
        return getReward(id).thenApply(x-> x.map(BasicReward::apply));
    }
    public CompletableFuture<Optional<Reward>> getReward(String id){
        return cache.get(id).thenApply(Optional::ofNullable);
    }

    public Reward makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new Reward()
                .id("memb-" + identifierCount)
                .spaceName("test-space-name")
                .created(OffsetDateTime.now())
                .customFields(java.util.Map.of("test-custom-field","test-val"))
                .addTagsItem("test-tag")
                .metadata(java.util.Map.of("test-metadata","test-val"))
                .name("Test-product")
                .description("Test-description")
                .addConstraintsItem("test-constraint")
                .delay(3)
                .icon("test-icon")
                .entityId("test-entity-id")
                .entityType(EntityType.ACHIEVEMENT)
                .issueLimit(3)
                .period(3)
                .pointInTime(OffsetDateTime.now())
                .rewardRank("test-reward-rank")
                .rewardType(new RewardTypeReduced().spaceName("test-space-name").id("reward-type-id"+identifierCount).key("test-key"));

    }

    @Override
    public CompletableFuture<? extends @NonNull Reward> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return CompletableFuture.completedFuture(makeMock());
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Reward value, RemovalCause cause) {

    }
}
