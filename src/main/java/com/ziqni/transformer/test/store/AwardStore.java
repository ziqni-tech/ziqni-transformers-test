package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicAward;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AwardStore implements AsyncCacheLoader<@NonNull String, @NonNull Award>, RemovalListener<@NonNull String, @NonNull Award> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();
    private static final Logger logger = LoggerFactory.getLogger(AwardStore.class);

    public final AsyncLoadingCache<@NonNull String, @NonNull Award> cache = Caffeine
            .newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public CompletableFuture<Optional<BasicAward>> getBasicAward(String id){
        return getAward(id).thenApply(x-> x.map(BasicAward::apply));
    }
    public CompletableFuture<Optional<Award>> getAward(String id){
        return cache.get(id).thenApply(Optional::ofNullable);
    }

    public Award makeMock(Reward reward){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new Award()
                .id("ach-" + identifierCount)
                .addTagsItem("tag-1").addTagsItem("tag-2").addTagsItem("tag-3")
                .metadata(Map.of("meta-1", "key-1"))
                .claimedTimestamp(OffsetDateTime.now())
                .constraints(List.of("constraint-1"))
                .created(OffsetDateTime.now())
                .delay(1)
                .entityId("Test-entity-id")
                .entityType(EntityType.ACHIEVEMENT)
                .memberId("Test-member-id")
                .memberRefId("Test-member-ref-id")
                .period(10)
                .pointInTime(OffsetDateTime.now())
                .rewardId("Test-reward-id")
                .rewardRank("1,2,3,4-7")
                .rewardValue(123455D)
                .rewardType(new RewardTypeReduced().id("Test-reward-type").key("test-key" + identifierCount).spaceName("test-space-1"))
                .spaceName("test-space-1");
    }

    @Override
    public CompletableFuture<? extends @NonNull Award> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return null;
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Award value, RemovalCause cause) {

    }
}
