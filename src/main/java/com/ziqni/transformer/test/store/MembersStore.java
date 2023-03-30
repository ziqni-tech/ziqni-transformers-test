package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.Member;
import com.ziqni.admin.sdk.model.MemberType;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicMember;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MembersStore implements AsyncCacheLoader<@NonNull String, @NonNull Member>, RemovalListener<@NonNull String, @NonNull Member> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();

    public final AsyncLoadingCache<@NonNull String, @NonNull Member> cache = Caffeine
            .newBuilder()
            .maximumSize(5_000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    /**
     * Get methods
     **/
    public CompletableFuture<Optional<String>> getIdByReferenceId(String memberRefId) {
        return null;
    }

    public CompletableFuture<String> getRefIdByMemberId(String memberId) {
        return cache.get(memberId).thenApply(Member::getMemberRefId);
    }

    public CompletableFuture<Optional<String>> create(String memberRefId, String displayName, scala.collection.Seq<String> tagsToCreate, scala.Option<scala.collection.Map<String, String>> metaData) {
        return null;
    }

    public CompletableFuture<Optional<Result>> update(String memberId, scala.Option<String> memberRefId, scala.Option<String> displayName, scala.Option<scala.collection.Seq<String>> tagsToUpdate, scala.Option<scala.collection.Map<String, String>> metaData) {
        return null;
    }

    public CompletableFuture<Optional<BasicMember>> findBasicMemberModelById(String memberId) {
        return findMemberById(memberId).thenApply(x -> x.map(BasicMember::apply));
    }

    public CompletableFuture<Optional<Member>> findMemberById(String memberId) {
        return cache.get(memberId).thenApply(Optional::ofNullable);
    }

    public Member makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new Member()
                .id("memb-" + identifierCount)
                .spaceName("test-space-name")
                .created(OffsetDateTime.now())
                .customFields(Map.of("new-field", "new-val"))
                .addTagsItem("test-tag")
                .metadata(Map.of("test-met", "test-key"))
                .name("test-name")
                .memberRefId("test-member-ref-id")
                .memberType(MemberType.INDIVIDUAL)
                .addTeamMembersItem("test-team-member")
                .addConstraintsItem("test-constraint-item")
                .timeZoneOffset("UTC");
    }

    @Override
    public CompletableFuture<? extends @NonNull Member> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return null;
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Member value, RemovalCause cause) {

    }
}