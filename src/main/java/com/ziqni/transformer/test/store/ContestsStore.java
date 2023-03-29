package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.Contest;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicContest;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ContestsStore implements AsyncCacheLoader<@NonNull String, @NonNull Contest>, RemovalListener<@NonNull String, @NonNull Contest> {

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

    @Override
    public CompletableFuture<? extends @NonNull Contest> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return null;
    }

    @Override
    public void onRemoval(@Nullable String key, @Nullable Contest value, RemovalCause cause) {

    }
}
