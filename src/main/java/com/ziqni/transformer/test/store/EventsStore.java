package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.EntityType;
import com.ziqni.admin.sdk.model.Member;
import com.ziqni.admin.sdk.model.ModelApiResponse;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.concurrent.ZiqniConcurrentHashMap;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.ZiqniMember;
import com.ziqni.transformer.test.utils.ScalaUtils;
import com.ziqni.transformers.domain.ZiqniEvent;
import lombok.NonNull;
import org.joda.time.DateTime;
import scala.None;
import scala.Option;
import scala.Some;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import scala.collection.immutable.Map$;
import scala.collection.immutable.Seq$;

public class EventsStore implements CacheLoader<@NonNull String, EventsStore.EventTransaction>, RemovalListener<@NonNull String, EventsStore.EventTransaction> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();

    private final ProductsStore productsStore;

    private final MembersStore membersStore;

    private final ActionTypesStore actionTypesStore;

    public final AsyncLoadingCache<String, EventsStore.EventTransaction> cache = Caffeine
            .newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .evictionListener(this).buildAsync(this);

    private final ZiqniConcurrentHashMap<String, List<ZiqniEvent>> batchIdCache = new ZiqniConcurrentHashMap<>();

    @Override
    public @Nullable EventsStore.EventTransaction load(@NonNull String key) throws Exception {
        return makeMock();
    }

    public EventsStore(ProductsStore productsStore, MembersStore membersStore, ActionTypesStore actionTypesStore, StoreContext context) {
        this.productsStore = productsStore;
        this.membersStore = membersStore;
        this.actionTypesStore = actionTypesStore;
    }

    public CompletableFuture<ModelApiResponse> pushEvent(ZiqniEvent basicEvent) {
        return pushEvent(List.of(basicEvent));
    }

    public CompletableFuture<ModelApiResponse> pushEvent(List<ZiqniEvent> basicEvents) {
        var response = new ModelApiResponse();
        basicEvents.forEach(x -> {
            pushEvent(x, response);
        });

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<ModelApiResponse> pushEventTransaction(ZiqniEvent basicEvent) {
        var response = new ModelApiResponse();
        pushEvent(basicEvent, response);

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<List<ZiqniEvent>> findByBatchId(String batchId) {
        return CompletableFuture.completedFuture(batchIdCache.get(batchId));

    }

    private void pushEvent(ZiqniEvent basicEvent, ModelApiResponse response) {
        if (basicEvent.action().equalsIgnoreCase(EntityType.MEMBER.getValue())){
            CompletableFuture<ZiqniMember> createdMember = membersStore.create(basicEvent.memberRefId(), "member-" + identifierCounter, basicEvent.tags(), Map$.MODULE$.empty());
            createdMember.thenAccept(y ->
                response.addResultsItem(new Result().id(y.getMemberId()))
            );

        } else if (basicEvent.action().equalsIgnoreCase(EntityType.PRODUCT.getValue())) {
            CompletableFuture<Optional<String>> createdProduct = productsStore.create(basicEvent.entityRefId(), "product-" + identifierCounter, ScalaUtils.emptySeqString, null, null, Map$.MODULE$.empty());
            createdProduct.thenAccept(y -> {
                y.ifPresent(z -> {
                    response.addResultsItem(new Result().id(z));
                });
            });
        }

        final var eventTransaction = new EventTransaction();
        eventTransaction.addZiqniEvent(basicEvent);

        basicEvent.batchId().map(batchId ->
                batchIdCache.put(batchId, eventTransaction.getEvents())
        );

        this.cache.put(basicEvent.eventRefId(), CompletableFuture.completedFuture(eventTransaction));
        response.addResultsItem(new Result().id(basicEvent.eventRefId()));

    }

    public EventTransaction makeMock(){
        final var eventTrans = new EventTransaction();
        String memberRefId = "member-ref-" + identifierCounter;
        AtomicReference<String> memberId = new AtomicReference<>();
        AtomicReference<String> action = new AtomicReference<>();
        var testEventName = new Some<>("test-event" + 1);
        final var createdMember = membersStore.create(memberRefId, "member-" + identifierCounter,  ScalaUtils.emptySeqString, Option.empty());
        createdMember.thenAccept(member -> memberId.set(member.getMemberId()));
        final var createdActionType = actionTypesStore.create("test-event", testEventName, Option.empty(), null);
        createdActionType.thenAccept(y -> {
            y.ifPresent(z -> action.set(z.getExternalReference()));
        });
        String batchId = "batch-" + identifierCounter;
        eventTrans.addZiqniEvent(new ZiqniEvent(new Some<>(memberId.get()), memberRefId, "ref-id-"+identifierCounter, "event-ref-id" + identifierCounter, new Some<>(batchId), action.get(), 2.0, DateTime.now(),  ScalaUtils.emptySeqString, Map$.MODULE$.empty()), Option.empty());
        this.batchIdCache.put(batchId, eventTrans.getEvents());
        return eventTrans;
    }

    @Override
    public void onRemoval(@NonNull String s, @NonNull EventTransaction eventTransaction, RemovalCause removalCause) {

    }


    public static class EventTransaction {
        private final List<ZiqniEvent> buffer = new ArrayList<>();

        public boolean addZiqniEvent(ZiqniEvent e) {
            return buffer.add(e);
        }

        public List<ZiqniEvent> getEvents() {
            return buffer;
        }
    }
}
