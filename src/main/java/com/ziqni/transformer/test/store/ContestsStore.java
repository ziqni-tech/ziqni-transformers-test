package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicContest;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ContestsStore implements AsyncCacheLoader<@NonNull String, @NonNull Contest>, RemovalListener<@NonNull String, @NonNull Contest> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();

    public final AsyncLoadingCache<@org.checkerframework.checker.nullness.qual.NonNull String, @org.checkerframework.checker.nullness.qual.NonNull Contest> cache = Caffeine
            .newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public CompletableFuture<Optional<BasicContest>> getBasicContestModel(String id) {
        return getContest(id).thenApply(contest -> contest.map(BasicContest::apply));
    }

    public CompletableFuture<Optional<Contest>> getContest(String id) {
        return cache.get(id).thenApply(Optional::ofNullable);
    }

    public Contest makeMock(Competition competition){
        final var identifierCount = identifierCounter.incrementAndGet();

        return new Contest()
                .id("ach-" + identifierCount)
                .addTagsItem("tag-1").addTagsItem("tag-2").addTagsItem("tag-3")
                .metadata(Map.of("meta-1", "key-1"))
                .description("Blah blah blah")
                .termsAndConditions("blah blah")
                .spaceName("test-space-1")
                .created(OffsetDateTime.now())
                .customFields(Map.of("key-1", "value-1"))
                .competitionId(competition.getId())
                .row(0)
                .name("Test-comp")
                .round(1)
                .roundType(RoundType.TIMEBOUND)
                .groupStage(1)
                .groupStageLabel("Test-group")
                .addEntrantsFromContestItem("Test-entrant")
                .maxNumberOfEntrants(10)
                .minNumberOfEntrants(1)
                .scheduledStartDate(OffsetDateTime.now())
                .scheduledEndDate(OffsetDateTime.now().plusDays(1))
                .strategies(new Strategy().rankingStrategy(new RankingStrategy().addConstraintsItem("test-ranking-strategy")))
                .status(ContestStatus.ACTIVE)
                .addConstraintsItem("test-constraint");
    }

    @Override
    public CompletableFuture<? extends @NonNull Contest> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return CompletableFuture.completedFuture(makeMock(new Competition().id("test-competition-id")));
    }

    @Override
    public void onRemoval(@Nullable String key, @Nullable Contest value, RemovalCause cause) {

    }
}
