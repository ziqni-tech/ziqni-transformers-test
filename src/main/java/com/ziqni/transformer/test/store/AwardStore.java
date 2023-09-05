package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.ZiqniAward;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AwardStore implements AsyncCacheLoader<@NonNull String, @NonNull Award>, RemovalListener<@NonNull String, @NonNull Award> {

    private static final Logger logger = LoggerFactory.getLogger(AwardStore.class);
    private final static AtomicInteger identifierCounter = new AtomicInteger();

    private final RewardStore rewardStore;

    public final AsyncLoadingCache<@NonNull String, @NonNull Award> cache = Caffeine
            .newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public AwardStore(RewardStore rewardStore, StoreContext context) {
        this.rewardStore = rewardStore;
    }

    public CompletableFuture<com.ziqni.transformer.test.models.ZiqniAward> getZiqniAward(String id){
        return getAward(id).thenApply(ZiqniAward::apply);
    }

    public CompletableFuture<Award> getAward(String id){
        return cache.get(id);
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
    public void onRemoval(@NonNull String key, @NonNull Award value, RemovalCause cause) {

    }
}
