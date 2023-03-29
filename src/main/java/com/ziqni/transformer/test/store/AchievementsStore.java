package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicAchievement;
import com.ziqni.admin.sdk.model.Achievement;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class AchievementsStore implements AsyncCacheLoader<@NonNull String, @NonNull Achievement>, RemovalListener<@NonNull String, @NonNull Achievement> {

    private static final Logger logger = LoggerFactory.getLogger(AchievementsStore.class);

    public final AsyncLoadingCache<@NonNull String, @NonNull Achievement> cache = Caffeine
            .newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(500)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public CompletableFuture<Optional<BasicAchievement>> getAchievement(String id) {
        return findAchievementById(id).thenApply(achievement -> achievement.map(BasicAchievement::apply));
    }

    public CompletableFuture<Optional<Achievement>> findAchievementById(String id) {
        return cache.get(id).thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<? extends @NonNull Achievement> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return null;
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Achievement value, RemovalCause cause) {

    }
}
