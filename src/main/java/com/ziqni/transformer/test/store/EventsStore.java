package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.EntityType;
import com.ziqni.admin.sdk.model.ModelApiResponse;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.concurrent.ZiqniConcurrentHashMap;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.ZiqniMember;
import com.ziqni.transformer.test.models.ZiqniProduct;
import com.ziqni.transformer.test.utils.ScalaUtils;
import com.ziqni.transformers.domain.ZiqniEvent;
import lombok.NonNull;
import org.joda.time.DateTime;
import scala.Option;
import scala.Some;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import scala.collection.immutable.Map$;

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

    public CompletableFuture<ModelApiResponse> pushEvent(ZiqniEvent event) {
        return pushEvent(List.of(event));
    }

    public CompletableFuture<ModelApiResponse> pushEvent(List<ZiqniEvent> event) {
        var response = new ModelApiResponse();
        event.forEach(x -> {
            pushEvent(x, response);
        });

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<ModelApiResponse> pushEventTransaction(ZiqniEvent ziqniEvent) {
        var response = new ModelApiResponse();
        pushEvent(ziqniEvent, response);

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<List<ZiqniEvent>> findByBatchId(String batchId) {
        return CompletableFuture.completedFuture(batchIdCache.get(batchId));

    }

    private void pushEvent(ZiqniEvent ziqniEvent, ModelApiResponse response) {
        if (ziqniEvent.action().equalsIgnoreCase(EntityType.MEMBER.getValue())){
            CompletableFuture<ZiqniMember> createdMember = membersStore.create(ziqniEvent.memberRefId(), "member-" + identifierCounter, ziqniEvent.tags(), Map$.MODULE$.empty());
            createdMember.thenAccept(y ->
                response.addResultsItem(new Result().id(y.getMemberId()))
            );

        } else if (ziqniEvent.action().equalsIgnoreCase(EntityType.PRODUCT.getValue())) {
            CompletableFuture<ZiqniProduct> createdProduct = productsStore.create(
                    ziqniEvent.entityRefId(),
                    "product-" + identifierCounter,
                    ScalaUtils.emptySeqString,
                    "productType",
                    1.0,
                    Map$.MODULE$.empty()
            );
            createdProduct.thenAccept(y ->
                response.addResultsItem(new Result().id(y.getProductId()))
            );
        }

        final var eventTransaction = new EventTransaction();
        eventTransaction.addZiqniEvent(ziqniEvent);

        ziqniEvent.batchId().map(batchId ->
                batchIdCache.put(batchId, eventTransaction.getEvents())
        );

        this.cache.put(ziqniEvent.eventRefId(), CompletableFuture.completedFuture(eventTransaction));
        response.addResultsItem(new Result().id(ziqniEvent.eventRefId()));

    }

    public EventTransaction makeMock(){
        final var eventTrans = new EventTransaction();
        String memberRefId = "member-ref-" + identifierCounter;
        AtomicReference<String> memberId = new AtomicReference<>();
        AtomicReference<String> action = new AtomicReference<>();
        var testEventName = new Some<>("test-event" + 1);
        final var createdMember = membersStore.create(memberRefId, "member-" + identifierCounter,  ScalaUtils.emptySeqString, Map$.MODULE$.empty());
        createdMember.thenAccept(member -> memberId.set(member.getMemberId()));
        final var createdActionType = actionTypesStore.create("test-event", testEventName, Option.empty(), null);
        createdActionType.thenAccept(actionTypeEntry ->
            action.set(actionTypeEntry.getKey())
        );
        String batchId = "batch-" + identifierCounter;
        eventTrans.addZiqniEvent(new ZiqniEvent(
                new Some<>(memberId.get()),
                memberRefId,
                "ref-id-"+identifierCounter,
                "event-ref-id" + identifierCounter,
                new Some<>(batchId),
                action.get(),
                2.0,
                DateTime.now(),
                ScalaUtils.emptySeqString,
                Map$.MODULE$.empty(),
                Option.empty()
        ));
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
