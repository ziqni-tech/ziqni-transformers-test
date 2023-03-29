package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.Award;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicAward;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class AwardStore implements AsyncCacheLoader<@NonNull String, @NonNull Award>, RemovalListener<@NonNull String, @NonNull Award> {

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

    @Override
    public CompletableFuture<? extends @NonNull Award> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return null;
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Award value, RemovalCause cause) {

    }
}
