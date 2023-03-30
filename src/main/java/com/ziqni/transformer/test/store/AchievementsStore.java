package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.*;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicAchievement;
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

public class AchievementsStore implements AsyncCacheLoader<@NonNull String, @NonNull Achievement>, RemovalListener<@NonNull String, @NonNull Achievement> {

    private static final Logger logger = LoggerFactory.getLogger(AchievementsStore.class);
    private final static AtomicInteger identifierCounter = new AtomicInteger();

    final String accountId;

    public final AsyncLoadingCache<@NonNull String, @NonNull Achievement> cache = Caffeine
            .newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(500)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public AchievementsStore(String accountId) {
        this.accountId = accountId;
    }

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

    public static Achievement makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new Achievement()
                .id("ach-" + identifierCount)
                .addTagsItem("tag-1").addTagsItem("tag-2").addTagsItem("tag-3")
                .metadata(Map.of("meta-1", "key-1"))
                .description("Blah blah blah")
                .termsAndConditions("blah blah")
                .icon("")
                //.achievementDependencies(new DependantOn().addMustNotItem("som other achievement id"))
                .scheduling(new Scheduling().scheduleType(ScheduleType.ONCE).startDate(OffsetDateTime.now()))
                .status(AchievementStatus.ACTIVE)
                //.category(List.of())
                .memberTagsFilter(new DependantOn().addMustItem("tag-1").addMustItem("tag-2").addMustItem("tag-3"))
                .name("My achievement test " + identifierCount)
                .maxNumberOfIssues(5)
                .constraints(List.of())
                ;
    }
}
