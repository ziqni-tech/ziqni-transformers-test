package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.ZiqniAward;
import com.ziqni.transformers.domain.ZiqniQueryResult;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;
import scala.jdk.CollectionConverters;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    public CompletableFuture<ZiqniQueryResult<com.ziqni.transformers.domain.ZiqniAward>> getAwardsByStatusCodeCreatedDate(int statusCodeFrom, int statusCodeTo, long skip, long limit, scala.Option<scala.Long> activeFrom, scala.Option<scala.Long> activeUntil, scala.Option<String> rewardTypeKey, scala.Option<String> memberId, scala.Option<String> rewardId, scala.Option<String> entityId){

        AtomicLong counterTotal = new AtomicLong(0);
        AtomicLong counterSkip = new AtomicLong(skip);
        AtomicLong counterLimit = new AtomicLong(limit);

        final var results =  this.cache.asMap().values().stream()
                .map( awardFuture ->
                        awardFuture.thenApply( award -> {
                            if(award.getRewardType().equals(rewardTypeKey.get()) && award.getMemberId().equals(memberId.get()) && award.getRewardId().equals(rewardId.get()) && award.getEntityId().equals(entityId.get())) {

                                counterTotal.incrementAndGet(); // increment total count
                                counterSkip.decrementAndGet(); // increment skip count

                                if (counterSkip.get() > 0 || counterLimit.get() <= 0) {
                                    return null;
                                }
                                else {
                                    counterLimit.decrementAndGet(); // decrement limit count
                                    return award;
                                }
                            }
                            else {
                                return null;
                            }
                        })
                )
                .filter(Objects::nonNull)
                .map(awardFuture -> awardFuture.thenApply(ZiqniAward::apply))
                .collect(Collectors.toList())
                ;

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                results.toArray(new CompletableFuture[0])
        );

        CompletableFuture<List<com.ziqni.transformers.domain.ZiqniAward>> allResultsFuture = allFutures.thenApply(v ->
                results.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        );

        List<com.ziqni.transformers.domain.ZiqniAward> finalResults = allResultsFuture.join();

        final Seq<com.ziqni.transformers.domain.ZiqniAward> out= CollectionConverters.ListHasAsScala(finalResults).asScala().toSeq();
        return CompletableFuture.completedFuture(new ZiqniQueryResult<>(finalResults.size(), finalResults.size(), skip, limit, out ));
    }

    public CompletableFuture<com.ziqni.transformer.test.models.ZiqniAward> updateAwardsState(String awardId, int action, List<String> constraints, String transactionReferenceId, String reasonForChange){
        return cache.get(awardId).thenApply( award ->
            award.statusCode(action).constraints(constraints)
        ).thenApply(a -> {
            cache.put(awardId, CompletableFuture.completedFuture(a) );
            return a;
        }).thenApply(ZiqniAward::apply);
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
