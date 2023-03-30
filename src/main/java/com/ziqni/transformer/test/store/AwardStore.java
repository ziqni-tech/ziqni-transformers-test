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

    private RewardStore rewardStore;

    public final AsyncLoadingCache<@NonNull String, @NonNull Award> cache = Caffeine
            .newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public AwardStore(RewardStore rewardStore) {
        this.rewardStore = rewardStore;
    }

    public CompletableFuture<Optional<BasicAward>> getBasicAward(String id){
        return getAward(id).thenApply(x-> x.map(BasicAward::apply));
    }
    public CompletableFuture<Optional<Award>> getAward(String id){
        return cache.get(id).thenApply(Optional::ofNullable);
    }

    public Award makeMock(Reward reward){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new Award()
                .id("award-" + identifierCount)
                .addTagsItem("tag-1").addTagsItem("tag-2").addTagsItem("tag-3")
                .metadata(reward.getMetadata())
                .claimedTimestamp(OffsetDateTime.now())
                .constraints(reward.getConstraints())
                .created(reward.getCreated())
                .delay(reward.getDelay())
                .entityId(reward.getEntityId())
                .entityType(reward.getEntityType())
                .memberId("Test-member-id")
                .memberRefId("Test-member-ref-id")
                .period(reward.getPeriod())
                .pointInTime(reward.getPointInTime())
                .rewardId(reward.getId())
                .rewardRank(reward.getRewardRank())
                .rewardValue(reward.getRewardValue())
                .rewardType(reward.getRewardType())
                .spaceName(reward.getSpaceName());
    }

    @Override
    public CompletableFuture<? extends @NonNull Award> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return CompletableFuture.completedFuture(makeMock(rewardStore.makeMock()));
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Award value, RemovalCause cause) {

    }
}
