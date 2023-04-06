package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.model.EntityType;
import com.ziqni.admin.sdk.model.ModelApiResponse;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.concurrent.ZiqniConcurrentHashMap;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformers.domain.BasicEventModel;
import lombok.NonNull;
import org.joda.time.DateTime;
import scala.Some;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    private final ZiqniConcurrentHashMap<String, List<BasicEventModel>> batchIdCache = new ZiqniConcurrentHashMap<>();

    @Override
    public @Nullable EventsStore.EventTransaction load(@NonNull String key) throws Exception {
        return makeMock();
    }

    public EventsStore(ProductsStore productsStore, MembersStore membersStore, ActionTypesStore actionTypesStore) {
        this.productsStore = productsStore;
        this.membersStore = membersStore;
        this.actionTypesStore = actionTypesStore;
    }

    public CompletableFuture<ModelApiResponse> pushEvent(BasicEventModel basicEventModel) {
        return pushEvent(List.of(basicEventModel));
    }

    public CompletableFuture<ModelApiResponse> pushEvent(List<BasicEventModel> basicEventModels) {
        var response = new ModelApiResponse();
        basicEventModels.forEach(x -> {
            pushEvent(x, response);
        });

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<ModelApiResponse> pushEventTransaction(BasicEventModel basicEventModel) {
        var response = new ModelApiResponse();
        pushEvent(basicEventModel, response);

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<List<BasicEventModel>> findByBatchId(String batchId) {
        return CompletableFuture.completedFuture(batchIdCache.get(batchId));

    }

    private void pushEvent(BasicEventModel basicEventModel, ModelApiResponse response) {
        if (basicEventModel.action().equalsIgnoreCase(EntityType.MEMBER.getValue())){
            CompletableFuture<Optional<String>> createdMember = membersStore.create(basicEventModel.memberRefId(), "member-" + identifierCounter, basicEventModel.tags(), null);
            createdMember.thenAccept(y -> {
                y.ifPresent(z -> {
                    response.addResultsItem(new Result().id(z));
                });
            });

        } else if (basicEventModel.action().equalsIgnoreCase(EntityType.PRODUCT.getValue())) {
            CompletableFuture<Optional<String>> createdProduct = productsStore.create(basicEventModel.entityRefId(), "product-" + identifierCounter, null, null, null, null);
            createdProduct.thenAccept(y -> {
                y.ifPresent(z -> {
                    response.addResultsItem(new Result().id(z));
                });
            });
        }

        final var eventTransaction = new EventTransaction();
        eventTransaction.addBasicEvent(basicEventModel);
        batchIdCache.put(basicEventModel.batchId().get(), eventTransaction.getEvents());
        this.cache.put(basicEventModel.eventRefId(), CompletableFuture.completedFuture(eventTransaction));
        response.addResultsItem(new Result().id(basicEventModel.eventRefId()));

    }

    public EventTransaction makeMock(){
        final var eventTrans = new EventTransaction();
        String memberRefId = "member-ref-" + identifierCounter;
        AtomicReference<String> memberId = new AtomicReference<>();
        AtomicReference<String> action = new AtomicReference<>();
        var testEventName = new Some<>("test-event" + 1);
        final var createdMember = membersStore.create(memberRefId, "member-" + identifierCounter, null, null);
        createdMember.thenAccept(y -> {
            y.ifPresent(memberId::set);
        });
        final var createdActionType = actionTypesStore.create("test-event", testEventName, null, null);
        createdActionType.thenAccept(y -> {
            y.ifPresent(z -> action.set(z.getExternalReference()));
        });
        String batchId = "batch-" + identifierCounter;
        eventTrans.addBasicEvent(new BasicEventModel(new Some<>(memberId.get()), memberRefId, null, "event-ref-id" + identifierCounter, new Some<>(batchId), action.get(), 2.0, DateTime.now(), null, null, null));
        this.batchIdCache.put(batchId, eventTrans.getEvents());
        return eventTrans;
    }

    @Override
    public void onRemoval(@NonNull String s, @NonNull EventTransaction eventTransaction, RemovalCause removalCause) {

    }


    public static class EventTransaction {
        private final List<BasicEventModel> buffer = new ArrayList<>();

        public boolean addBasicEvent(BasicEventModel e) {
            return buffer.add(e);
        }

        public List<BasicEventModel> getEvents() {
            return buffer;
        }
    }
}
