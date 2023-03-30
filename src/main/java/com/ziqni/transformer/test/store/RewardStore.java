package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicReward;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class RewardStore implements AsyncCacheLoader<@NonNull String, @NonNull Reward>, RemovalListener<@NonNull String, @NonNull Reward> {

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

    }

    @Override
    public CompletableFuture<? extends @NonNull Reward> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return null;
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Reward value, RemovalCause cause) {

    }
}
