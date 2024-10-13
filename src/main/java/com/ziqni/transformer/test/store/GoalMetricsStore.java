package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.Award;
import com.ziqni.admin.sdk.model.GoalMetrics;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.domain.MockGoalMetric;
import com.ziqni.transformers.domain.ZiqniGoalMetric;
import scala.collection.immutable.Map;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class GoalMetricsStore implements AsyncCacheLoader< GoalMetricsStore.GoalMetricKey, GoalMetrics>, RemovalListener<GoalMetricsStore.GoalMetricKey, GoalMetrics> {


    public final Random random = new Random();

    public final AsyncLoadingCache< GoalMetricsStore.GoalMetricKey, GoalMetrics> cache = Caffeine
            .newBuilder()
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);
    private final AwardStore awardStore;
    private final StoreContext context;

    public GoalMetricsStore(AwardStore awardStore, StoreContext context) {
        this.awardStore = awardStore;
        this.context = context;
    }

    public CompletableFuture<List<ZiqniGoalMetric>> getGoalMetrics(List<String> memberIds, List<String> entityIds){
        final var keys = memberIds.stream().flatMap(memberId -> entityIds.stream().map(entityId -> new GoalMetricKey(memberId, entityId))).toList();
        return this.cache
                .getAll(keys)
                .thenApply( metricsMap ->
                        metricsMap.values().stream().map(y -> new MockGoalMetric(y).asZiqniGoalMetric()).toList()
                );
    }

    @Override
    public CompletableFuture<? extends GoalMetrics> asyncLoad(GoalMetricsStore.GoalMetricKey key, Executor executor) throws Exception {
        return this.awardStore.getAward(key.toString()).thenApply(this::makeMock);
    }

    public GoalMetrics addGoalMetrics(Award award){
        final var key = new GoalMetricKey(award.getEntityId(),award.getMemberId());
        final var goalMetrics = makeMock(award);
        cache.put(key, CompletableFuture.completedFuture(goalMetrics));
        return goalMetrics;
    }

    public GoalMetrics makeMock(Award award){
        return new GoalMetrics()
                .accountId(this.context.getAccountId())
                .memberId(award.getMemberId())
                .entityId(award.getEntityId())
                .value(BigDecimal.valueOf(random.nextDouble()))
                .percentageComplete(0.0)
                .mostSignificantScores(null)
                .timestamp(null)
                .updateCount(random.nextLong())
                .entityType(award.getEntityType().toString())
                .markerTimeStamp(null)
                .goalReached(true)
                .statusCode(35)
                .position(0)
                .userDefinedValues(new HashMap<>())
                ;
    }

    @Override
    public void onRemoval(GoalMetricKey key, GoalMetrics value, RemovalCause cause) {

    }

    public record GoalMetricKey(String memberId, String entityId) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GoalMetricKey that)) return false;
            return Objects.equals(memberId, that.memberId) && Objects.equals(entityId, that.entityId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(memberId, entityId);
        }

        @Override
        public String toString() {
            return memberId + entityId;
        }
    }
}
