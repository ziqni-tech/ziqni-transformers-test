package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.ApiException;
import com.ziqni.admin.sdk.model.Member;
import com.ziqni.admin.sdk.model.MemberType;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.concurrent.ZiqniConcurrentHashMap;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicMember;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import scala.collection.JavaConverters;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MembersStore implements AsyncCacheLoader<@NonNull String, @NonNull Member>, RemovalListener<@NonNull String, @NonNull Member> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();

    private final ZiqniConcurrentHashMap<String, String> refIdCache = new ZiqniConcurrentHashMap<>();

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
        return this.refIdCache.getAsync(memberRefId);
    }

    public CompletableFuture<String> getRefIdByMemberId(String memberId) {
        return cache.get(memberId).thenApply(Member::getMemberRefId);
    }

    public CompletableFuture<Optional<String>> create(String memberRefId, String displayName, scala.collection.Seq<String> tagsToCreate, scala.Option<scala.collection.Map<String, String>> metaData) {
        final var out = new CompletableFuture<Optional<String>>();
        if(this.refIdCache.containsKey(memberRefId))
            out.completeExceptionally(new ApiException("member_ref_id_already_exists")); // or whatever we throw
        else {
            final var tags = JavaConverters.seqAsJavaList(tagsToCreate);
            final var metadata = JavaConverters.mapAsJavaMap(metaData.get());
            final var member = makeMock()
                    .name(displayName)
                    .memberRefId(memberRefId)
                    .tags(tags)
                    .metadata(metadata);
            this.cache.put(member.getId(), CompletableFuture.completedFuture(member));
            this.refIdCache.put(member.getMemberRefId(), member.getId());
            out.thenApply(x -> x.orElse(member.getId()));
        }

        return out;

    }

    public CompletableFuture<Optional<Result>> update(String memberId, scala.Option<String> memberRefId, scala.Option<String> displayName, scala.Option<scala.collection.Seq<String>> tagsToUpdate, scala.Option<scala.collection.Map<String, String>> metaData) {
        final var out = new CompletableFuture<Optional<Result>>();
        var isNotInCache = this.cache
                .get(memberId)
                .thenApply(Objects::isNull)
                .join();
        if(isNotInCache)
            out.completeExceptionally(new ApiException("member_with_id_[" + memberId + "]_does_not_exist")); // or whatever we throw
        else {
            this.cache.getIfPresent(memberId)
                    .thenApply(x -> {
                if (!memberRefId.isEmpty())
                    x.memberRefId(memberRefId.get());
                if (!displayName.isEmpty())
                    x.name(displayName.get());
                if (!tagsToUpdate.isEmpty())
                    x.tags(JavaConverters.seqAsJavaList(tagsToUpdate.get()));
                if (!metaData.isEmpty())
                    x.metadata(JavaConverters.mapAsJavaMap(metaData.get()));

                out.thenApply(y -> y.orElse(new Result()
                        .id(x.getId())
                        .result("UPDATED")
                        .externalReference(x.getMemberRefId())));

                return x;
            });

        }

        return out;
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
                .name("test-name-"+identifierCount)
                .memberRefId("test-member-ref-id"+identifierCount)
                .memberType(MemberType.INDIVIDUAL)
                .addTeamMembersItem("test-team-member")
                //.addConstraintsItem("test-constraint-item")
                .timeZoneOffset("UTC");
    }

    @Override
    public CompletableFuture<? extends @NonNull Member> asyncLoad(@NonNull String key, Executor executor) throws Exception {
       return CompletableFuture.completedFuture(makeMock()).thenApply(member -> {
           this.refIdCache.put(member.getMemberRefId(), member.getId());
           return member;
       });
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Member value, RemovalCause cause) {

    }
}
