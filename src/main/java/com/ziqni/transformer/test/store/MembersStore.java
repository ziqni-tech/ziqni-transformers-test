package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.ApiException;
import com.ziqni.admin.sdk.model.Member;
import com.ziqni.admin.sdk.model.MemberType;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.concurrent.ZiqniConcurrentHashMap;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.ZiqniMember;
import com.ziqni.transformer.test.models.ZiqniProduct;
import com.ziqni.transformers.ZiqniNotFoundException;
import com.ziqni.transformers.domain.CreateMemberRequest;
import org.checkerframework.checker.nullness.qual.NonNull;
import scala.collection.JavaConverters;
import scala.collection.immutable.Map$;

import java.time.OffsetDateTime;
import java.util.*;
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

    public MembersStore(StoreContext context) {
    }

    /**
     * Get methods
     **/
    public CompletableFuture<ZiqniMember> getMemberByReferenceId(String memberRefId) {
        return this.refIdCache.getAsync(memberRefId).thenCompose(refId -> {
            if (refId.isPresent())
                return this.cache.get(refId.get()).thenApply(ZiqniMember::apply);
            else
                return CompletableFuture.failedFuture(new ZiqniNotFoundException("ZiqniMember", "null", true));
        });
    }

    public CompletableFuture<String> getRefIdByMemberId(String memberId) {
        return cache.get(memberId).thenApply(Member::getMemberRefId);
    }

    public CompletableFuture<ZiqniMember> create(String memberRefId, String displayName, scala.collection.immutable.Seq<String> tagsToCreate, scala.collection.immutable.Map<String, String> metaData) {
        return create(new CreateMemberRequest(memberRefId,displayName,tagsToCreate, Map$.MODULE$.empty(),metaData));
    }
    public CompletableFuture<ZiqniMember> create(com.ziqni.transformers.domain.CreateMemberRequest toCreate) {

        final var out = new CompletableFuture<Member>();
        if(this.refIdCache.containsKey(toCreate.memberReferenceId()))
            out.completeExceptionally(new ApiException("member_ref_id_already_exists")); // or whatever we throw
        else {
            final var tags = Objects.nonNull(toCreate.tags()) && !toCreate.tags().isEmpty() ? JavaConverters.seqAsJavaList(toCreate.tags()) : new ArrayList<String>();
            final var metadata = Objects.nonNull(toCreate.metadata()) && !toCreate.metadata().isEmpty() ? JavaConverters.mapAsJavaMap(toCreate.metadata()) : new HashMap<String, String>();
            final var member = makeMock()
                    .name(toCreate.displayName())
                    .memberRefId(toCreate.memberReferenceId())
                    .tags(tags)
                    .metadata(metadata);
            this.cache.put(member.getId(), CompletableFuture.completedFuture(member));
            this.refIdCache.put(member.getMemberRefId(), member.getId());
            out.complete(member);
        }

        return out.thenApply(ZiqniMember::new);

    }

    public CompletableFuture<ZiqniMember> update(String memberId, scala.Option<String> memberRefId, scala.Option<String> displayName, scala.Option<scala.collection.Seq<String>> tagsToUpdate) {
        final var out = new CompletableFuture<ZiqniMember>();
        var isNotInCache = Objects.isNull(this.cache.getIfPresent(memberId));
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

                out.complete(new ZiqniMember(x));
                return x;
            });

        }

        return out;
    }

    public CompletableFuture<ZiqniMember> findZiqniMemberById(String memberId) {
        return findMemberById(memberId).thenApply(x -> x.map(ZiqniMember::apply).orElseThrow(() -> new ZiqniNotFoundException("ZiqniMember",memberId,false)));
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
    public void onRemoval(@NonNull String key, @NonNull Member value, RemovalCause cause) {

    }
}
